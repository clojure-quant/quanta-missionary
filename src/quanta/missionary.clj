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
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))

(def run-parallel
  quanta.missionary.parallel/run-parallel)
