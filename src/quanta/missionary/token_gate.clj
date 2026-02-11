(ns quanta.missionary.token-gate
  (:require
   [missionary.core :as m]))

(defn refill [{:keys [tokens last-ts rate capacity] :as st}]
  (let [t (System/currentTimeMillis)
        elapsed-ms (max 0 (- t last-ts))
        add (* rate (/ elapsed-ms 1000.0))
        new-tokens (min capacity (+ tokens add))]
    (assoc st :tokens new-tokens :last-ts t)))

(defn bucket-delay-ms [st cost]
  (let [{:keys [tokens rate]} st
        deficit (max 0.0 (- cost tokens))]
    (long (Math/ceil (* 1000.0 (/ deficit rate))))))

(defn consume [st cost]
  (update st :tokens (fn [x] (- x cost))))

(defn token-bucket-gate
  "Wrap a requests flow, delaying items until tokens available.
   capacity: maximum available tokens (relevant for initial burst)
   capacity: allowed max tokens/second
   cost: token cost per request (default 1)"
  [requests {:keys [capacity rate cost]
             :or {capacity 20
                  rate 5
                  cost 1}}]
  (m/ap (let [capacity (double capacity)
              rate (double rate)
              cost (double cost)
              st (atom {:tokens capacity
                        :last-ts (System/currentTimeMillis)
                        :rate rate
                        :capacity capacity})]
          (println "st: " st)
          (let [job (m/?> 1 requests)]
            (println "got job: " job)
            (let [st1 (refill @st)
                  d   (bucket-delay-ms st1 cost)]
              (when (pos? d)
                (println "sleeping " d)
                (m/? (m/sleep d)))
              (let [st2 (consume (refill st1) cost)]
                (println "next")
                (reset! st st2)
                job))))))
