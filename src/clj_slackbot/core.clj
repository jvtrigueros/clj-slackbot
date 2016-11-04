 (ns clj-slackbot.core
   (:require [clj-slackbot.config :as config]
             [clj-slackbot.util :as util]
             [clj-slackbot.evaluator :as evaluator]
             [clojure.core.async :as async :refer [>! <! go go-loop]]
             [slack-overwatch.core :as overwatch])
   (:import java.lang.Thread)
  (:gen-class))

 (defn make-comm [id config]
   (let [f (util/kw->fn id)]
     (f config)))

 (defn -main [& args]
  (let [config (config/read-config)
        inst-comm (fn []
                    (println ":: building com:" (:comm config))
                    (make-comm (:comm config) config))]
    (println ":: starting with config:" config)

    (go-loop [[in out stop] (inst-comm)]
      (println ":: waiting for input")
      (if-let [form (<! in)]
        (let [gamer-tag (:input form)
              res (overwatch/player-profile gamer-tag)]
          (println ":: form >> " gamer-tag)
          (println ":: => " res)
          (>! out (assoc form :profile res))
          (recur [in out stop]))

        ;; something wrong happened, re init
        (do
          (println ":: WARNING! The comms went down, going to restart.")
          (stop)
          (<! (async/timeout 3000))
          (recur (inst-comm)))))

    (.join (Thread/currentThread))))

