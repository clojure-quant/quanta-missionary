(ns demo.gate
  (:require
   [missionary.core :as m]
   [quanta.missionary :refer [token-bucket-gate]]))

(def requests
  (m/seed (range 120) ;(map do-request (range 100))
          ))

(def responses
  (token-bucket-gate requests {:capacity 20
                               :rate 5
                               :cost 1}))

; first 20 - immediate
; thereafter 5/second

; 50 requests = 20 immediate, 30 throttled 30/5=6 

(time
 (m/? (m/reduce conj  (m/eduction
                       (take 50) responses))))

; "Elapsed time: 6005.964577 msecs"

(defn do-request [job]
  (m/sp
   (m/? (m/sleep 1000))
   ;; pretend rate-limit decreasing
   {:status 200
    :headers {"x-ratelimit-remaining"
              (str (max 0 (- 20 job)))
              "x-ratelimit-reset" "30"}}))

(def requests-task-seq
  (m/seed (map do-request (range 100))))
(def responses-tasks
  (token-bucket-gate requests-task-seq {:capacity 20
                                        :rate 5
                                        :cost 1}))
(time
 (m/? (m/reduce conj  (m/eduction
                       (take 50) responses-tasks))))

