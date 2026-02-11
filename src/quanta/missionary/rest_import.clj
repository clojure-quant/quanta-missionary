(ns quanta.missionary.rest-import
  (:require
   [missionary.core :as m]
   [quanta.missionary.token-gate :refer [token-bucket-gate]]))

(defn import-one [ctx {:keys [download-fn store-fn summary-fn]} job-opts]
  (m/sp
   (try
     (let [data (m/? (download-fn ctx job-opts))]
       (when store-fn
         (m/? (store-fn ctx job-opts data)))
       {:opts job-opts
        :data (if summary-fn
                (summary-fn data)
                :success)})
     (catch Exception ex
       {:opts job-opts
        :error ex}))))

(defn process-jobs [ctx opts job-f]
  (m/ap
   (let [job-opts (m/?> (:parallel opts) job-f)]
     (m/? (import-one ctx (dissoc opts :tasks-opts :parallel) job-opts)))))

(defn summarize-result [s result]
  (-> s
      (update :success (fn [n] (if (:data result) (inc n) n)))
      (update :error (fn [n] (if (:error result) (inc n) n)))
      (update :error-details (fn [n] (if (:error result) (conj n (:opts result)) n)))))

(defn rest-import [ctx {:keys [tasks-opts download-fn store-fn
                               capacity rate cost
                               parallel]
                        :as opts}]
  (assert (or (vector? tasks-opts) (seq? tasks-opts)) ":tasks-opts needs to be seq or vector")
  (assert download-fn ":download-fn needs to be a function that returns a sp")
  (when store-fn (assert (fn? store-fn)) ":store-fn needs to be a function (or nil)")
  (assert (int? capacity) ":capacity needs to be an integer")
  (assert (int? rate) ":rate needs to be an integer")
  (assert (int? cost) ":cost needs to be an integer")
  (assert (int? parallel) ":parallel needs to be an integer")
  (let [job-opts-f (token-bucket-gate (m/seed tasks-opts) (select-keys opts [:capacity :rate :cost]))
        rest-opts (dissoc opts :capacity :rate :cost)
        job-f (process-jobs ctx rest-opts job-opts-f)]
    (m/reduce summarize-result {:success 0 :error 0 :error-details []} job-f)))
