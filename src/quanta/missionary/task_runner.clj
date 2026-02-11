(ns quanta.missionary.task-runner
  (:require
   [missionary.core :as m]))

(defonce running-tasks (atom {}))

(defn start!
  "starts a missionary task
   task can be stopped with (stop! task-id).
   useful for working in the repl with tasks"
  [task id]
  (if-let [_ (get @running-tasks id)]
    (println "ERROR: task " id " already started!")
    (let [dispose! (task
                    #(println "task completed: " %)
                    #(println "task crashed: " %))]
      (swap! running-tasks assoc id dispose!)
      (str "task " id " started!"))))

(defn stop!
  "stops a missionary task that has been started with start!
    useful for working in the repl with tasks"
  [id]
  (if-let [dispose! (get @running-tasks id)]
    (do (swap! running-tasks dissoc id)
        (dispose!))
    (println "cannot stop task - not existing!" id)))

(defn start-flow-printer!
  "starts printing a missionary flow to the console.
   printing can be stopped with (stop! id) 
   useful for working in the repl with flows."
  [f id]
  (let [print-task (m/reduce (fn [_r v]
                               (println id " " v)
                               nil)
                             nil f)]
    (start! print-task id)))

(defn start-flow-logger!
  "starts logging a missionary flow to a file.
   can be stopped with (stop! id) 
   useful for working in the repl with flows."
  [file-name id f]
  (let [log-task (m/reduce (fn [r v]
                             (let [s (with-out-str (println v))]
                               (spit file-name s :append true))
                             nil)
                           nil f)]
    (start! log-task id)))