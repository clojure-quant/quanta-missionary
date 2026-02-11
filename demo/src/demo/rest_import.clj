(ns demo.rest-import
  (:require
   [missionary.core :as m]
   [quanta.missionary :refer [rest-import]]))

(def ctx {:service-a 1})

(defn do-request [ctx opts]
  (m/sp
   (m/? (m/sleep 1000))
   (when (< (rand-int 30) 2)
     (throw (ex-info "server offline" opts)))
   ;; pretend rate-limit decreasing
   {:status 200
    :headers {"x-ratelimit-remaining"
              (str (max 0 (- 20 opts)))
              "x-ratelimit-reset" "30"}}))

(m/? (rest-import ctx {:tasks-opts (range 100)
                       :download-fn do-request
                       :parallel 50
                       :cost 1
                       :capacity 50
                       :rate 10}))

