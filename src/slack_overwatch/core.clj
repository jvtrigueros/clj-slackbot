(ns slack-overwatch.core
  (:require [cheshire.core :refer [parse-string]]
            [clojure.string :as str]
            [clj-http.client :as client]))

(def sample-attachment
  [{:pretext     "Competitive ranking for *trigoman*."
    :mrkdwn_in   ["pretext"]
    :author_name "trigoman#1562"
    :author_link "https://api.lootbox.eu/pc/us/trigoman-1562/profile"
    :author_icon "https://blzgdapipro-a.akamaihd.net/game/unlocks/0x02500000000007D1.png"
    :thumb_url   "https://blzgdapipro-a.akamaihd.net/game/rank-icons/season-2/rank-3.png"
    :fields      [{:title "Rank"
                   :value "2007"
                   :short true}]}
   {:fields [{:title "Level"
              :value "120"
              :short true}
             {:title "Win/Loss"
              :value "64/73"
              :short true}]
    :ts     1478149069}])

(def overwatch-api "https://api.lootbox.eu")

(defn player-profile [gamer-tag]
  (let [platform "pc"
        region "us"
        tag (str/replace gamer-tag #"#" "-")
        operation "profile"]
    (parse-string
      (:body
        (client/get
          (str/join "/" [overwatch-api platform region tag operation])
          {:insecure? true}))
      true)))

