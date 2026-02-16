(ns demo.parallel
  (:require
   [missionary.core :as m]
   [quanta.missionary :refer [run-parallel]]))

(defn random-task []
  (m/sp
   (rand-int 100)))

(def tasks (repeatedly 5 random-task))

(count tasks)

(m/? (run-parallel tasks 8))
