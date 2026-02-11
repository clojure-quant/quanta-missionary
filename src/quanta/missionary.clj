(ns quanta.missionary
  (:require
   [quanta.missionary.token-gate]
   [quanta.missionary.backoff]))

(def token-bucket-gate quanta.missionary.token-gate/token-bucket-gate)

(def backoff quanta.missionary.backoff/backoff)

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(defn mix
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))
