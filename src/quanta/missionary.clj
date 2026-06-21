(ns quanta.missionary
  (:require
   [missionary.core :as m]
   [quanta.missionary.token-gate]
   [quanta.missionary.backoff]
   [quanta.missionary.rest-import]
   [quanta.missionary.parallel]))

(def token-bucket-gate quanta.missionary.token-gate/token-bucket-gate)

(def backoff quanta.missionary.backoff/backoff)

(def rest-import quanta.missionary.rest-import/rest-import)

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(defn mix
  "Return a flow which is mix of all input flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))

(defn mix-tagged
  "flow map is a map whose keywords are identitifiers for the flows
   (the values). the mixed flow will emit vectors, the first item
   in the vector is the keyword, the second item is the value of 
   the flow."
  [flow-map]
  (let [mapped-flows (map (fn [[k f]]
                            (m/ap (let [data (m/?> f)]
                                    [k data]))) flow-map)]
    (apply mix mapped-flows)))

(def run-parallel
  quanta.missionary.parallel/run-parallel)
