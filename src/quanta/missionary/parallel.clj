(ns quanta.missionary.parallel
  (:require
   [missionary.core :as m]))

(defn- limit-task [sem blocking-task]
  (m/sp
   (m/holding sem (m/? blocking-task))))

(defn- run-tasks
  "runs multiple tasks"
  [tasks parallel-nr]
  ; from: https://github.com/leonoel/missionary/wiki/Rate-limiting#bounded-blocking-execution
  ; When using (via blk ,,,) It's important to remember that the blocking thread pool 
  ; is unbounded, which can potentially lead to out-of-memory exceptions. 
  ; A simple way to work around it is by using a semaphore to rate limit the execution:
  (let [sem (m/sem parallel-nr)
        tasks-limited (map #(limit-task sem %) tasks)
        summarize (fn [& args] args)]
    (apply m/join summarize tasks-limited)))