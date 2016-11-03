(ns slack-overwatch.core)

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
