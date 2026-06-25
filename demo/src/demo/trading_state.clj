(ns demo.trading-state
  (:require
   [missionary.core :as m]
   [quanta.missionary.time-flow :refer [create-time-flow]]
   [quanta.missionary :refer [mix mix-tagged]]))

(def trade-f
  (create-time-flow
   [0 {:buy  50}
    0 {:buy  50}
    1 {:buy  50}
    0 {:buy 150}
    5 {:sell 250}
    0 {:sell  50}
    4 {:buy 400}]))

(def order-state-f
  (create-time-flow
   [0 [{:order/id 1 :side :buy :qty 100 :status :created}]
    4 [{:order/id 1 :side :buy :qty 100 :status :filled}
       {:order/id 2 :side :buy :qty 200 :status :created}]
    2 [{:order/id 2 :side :buy :qty 200 :status :filled}
       {:order/id 3 :side :buy :qty 300 :status :created}]
    2 [{:order/id 3 :side :buy :qty 300 :status :filled}
       {:order/id 4 :side :buy :qty 400 :status :created}]]))

(def tagged-flow
  (mix-tagged {:trade trade-f
               :order-state order-state-f}))

(defn acc-state [state [k v]]
  (case k
    :trade (update state :trade conj v)
    :order-state (assoc state :order-state v)))

((->> (m/ap
       (let [[_ batch] (m/?> (m/group-by {}
                                         ;(m/ap (m/?> (m/amb= trade-f order-state-f)))
                                         tagged-flow))]
         (m/? (->> (m/ap (m/amb= (m/?> batch)
                                 (m/? (m/sleep 50))))
                   (m/eduction (take-while some?))
                   ;(m/reduce conj {:trade []})
                   (m/reduce acc-state {:trade []})))))
      (m/eduction (take 20))
      (m/reduce conj))
 prn prn)

[{:trade [{:buy 100}],
  :order-state [{:order/id 1, :side :buy, :qty 100, :status :created}]}

 {:trade [{:buy 200}]}

 {:trade [],
  :order-state [{:order/id 1, :side :buy, :qty 100, :status :filled}
                {:order/id 2, :side :buy, :qty 200, :status :created}]}

 {:trade [{:sell 300}]}
 {:trade [],
  :order-state [{:order/id 2, :side :buy, :qty 200, :status :filled}
                {:order/id 3, :side :buy, :qty 300, :status :created}]}
 {:trade [{:buy 400}]}
 {:trade [],
  :order-state [{:order/id 3, :side :buy, :qty 300, :status :filled}
                {:order/id 4, :side :buy, :qty 400, :status :created}]}]

[{:trade [{:buy 50} {:buy 50}],
  :order-state [{:order/id 1, :side :buy, :qty 100, :status :created}]}

 {:trade [{:buy 50} {:buy 150}]}

 {:trade [],
  :order-state [{:order/id 1, :side :buy, :qty 100, :status :filled}
                {:order/id 2, :side :buy, :qty 200, :status :created}]}
 {:trade [{:sell 250} {:sell 50}],
  :order-state [{:order/id 2, :side :buy, :qty 200, :status :filled}
                {:order/id 3, :side :buy, :qty 300, :status :created}]}

 {:trade [],
  :order-state [{:order/id 3, :side :buy, :qty 300, :status :filled}
                {:order/id 4, :side :buy, :qty 400, :status :created}]}

 {:trade [{:buy 400}]}]