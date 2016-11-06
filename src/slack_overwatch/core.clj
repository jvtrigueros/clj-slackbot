(ns slack-overwatch.core
  (:require [cheshire.core :refer [parse-string]]
            [clojure.string :as str]
            [clj-http.client :as client])
  (:import (clojure.lang ExceptionInfo)))

(def overwatch-api "http://ow-api.herokuapp.com")

(defn- create-field [title value short?]
  {:title title
   :value value
   :short short?})

(defn profile->attachment
  [data]
  (if (map? data)
    (let [author-name (:username data)
          ;author-icon (:avatar data)
          {:keys [rank rank_img]} (:competitive data)
          level (:level data)
          {:keys [wins lost]} (get-in data [:games :competitive])]
      [{:pretext     (str "Competitive ranking for *" author-name "*!")
        :mrkdwn_in   ["pretext"]
        :author_name author-name
        ;:author_icon author-icon
        :thumb_url   rank_img
        :fields      [(create-field "Rank" rank true)
                      (create-field "Win/Loss" (str/join "/" [wins lost]) true)]}
       #_{:fields [(create-field "Level" level true)
                   (create-field "Win/Loss" (str/join "/" [wins lost]) true)]}
       :ts (quot (System/currentTimeMillis) 1000)])
    [{:pretext data
      :mrkdwn_in ["pretext"]}]))

(defn player-profile [gamer-tag]
  (let [platform "pc"
        region "us"
        tag (str/replace gamer-tag #"#" "-")
        operation "profile"]
    (try
      (parse-string
        (:body
          (client/get
            (str/join "/" [overwatch-api operation platform region tag])))
        true)
      (catch ExceptionInfo _ (str "Could not find *" gamer-tag "*. Verify case and/or tag number.")))))
