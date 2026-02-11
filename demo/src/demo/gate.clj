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


