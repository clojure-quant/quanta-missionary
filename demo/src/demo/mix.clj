(ns demo.mix
  (:require
   [missionary.core :as m]
   [quanta.missionary.time-flow :refer [create-time-flow]]
   [quanta.missionary :refer [mix mix-tagged]]))

(def k-f
  (create-time-flow
   [0 :a
    5 :b
    5 :c
    5 :d]))

(def i-f
  (create-time-flow
   [0 1
    5 2
    5 3
    5 4]))

(defn start-printer [f]
  (let [printer (m/reduce
                 (fn [r v]
                   (println v)
                   nil)
                 nil
                 f)]
    (println "printer starting..")
    (printer #(prn "printer finished " %) #(prn "printer crashed " %))
    nil))

(start-printer (m/seed [1 2 3]))

(def mixed-simple (mix i-f k-f))

(start-printer mixed-simple)

(def tagged-mixed-f (mix-tagged {:k k-f :i i-f}))

(start-printer tagged-mixed-f)




