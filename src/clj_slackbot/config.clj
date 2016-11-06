(ns clj-slackbot.config
  (:require [clojure.edn :as edn]
            [environ.core :refer [env]]))

(defn string->int [^String string]
  (try
    (Integer/parseInt string)
    (catch NumberFormatException _ nil)))

(defn read-config []
  (let [path (or (:config-file env)
                 "config.edn")
        config (edn/read-string (slurp path))]
    ; Needed for Heroku to work, they provide a port from the environment.
    (assoc config :port
                  (or
                    (string->int (env :port))
                    (:port config)))))
