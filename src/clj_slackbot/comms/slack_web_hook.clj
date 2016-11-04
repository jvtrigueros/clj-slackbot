(ns clj-slackbot.comms.slack-web-hook
  (:require [clj-slackbot.util :as util]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.core.async :as async :refer [>!! <!! go-loop]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [org.httpkit.server :refer [run-server]]
            [slack-overwatch.core :as overwatch])
  (:gen-class))

(defn post-to-slack
  ([post-url s]
   (client/post post-url
                {:content-type :json
                 :form-params  {:text          (get-in s [:data :competitive :rank])
                                :response_type "in_channel"}})))

(defn handle-clj [params command-token cin]
  (if-not (= (:token params) command-token)
    {:status 403 :body "Unauthorized"}
    (let [{:keys [response_url user_name text]} params]

      ;; send the form to our evaluator and get out of here
      (>!! cin {:input text
                :meta  {:response-url response_url
                        :user         user_name}})

      {:status 200 :body (str "Calculating stats for " text) :headers {"Content-Type" "text/plain"}})))

(defn start [{:keys [port command-token]}]
  ;; check we have everything
  (when (some nil? [port command-token])
    (throw (Exception. "Cannot initialize. Missing port or command-token")))

  (println ":: starting http server on port:" port)
  (let [cin (async/chan 10)
        cout (async/chan 10)
        app (-> (routes
                  (POST "/overwatch" req (handle-clj (:params req)
                                                     command-token
                                                     cin))
                  (route/not-found "Not Found"))
                (wrap-defaults api-defaults))]
    ;; start the loops we need to read back eval responses
    (go-loop [res (<!! cout)]
      (if-not res
        (println "The form output channel has been closed. Leaving listen loop.")
        (let [post-url (get-in res [:meta :response-url])
              profile (:profile res)]
          (post-to-slack
            post-url
            profile
            #_(util/format-result-for-slack res))
          (recur (<!! cout)))))

    ;; start web listener
    (let [server (run-server app {:port port})]
      [cin cout (fn []
                  (async/close! cin)
                  (async/close! cout)
                  (server))])))


