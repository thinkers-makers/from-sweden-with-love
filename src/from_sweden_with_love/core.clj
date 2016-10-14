(ns from-sweden-with-love.core
  (:require [twitter-streaming-client.core :as client]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as rest]
            [twitter.request :as req]
            [clojure.set :as set]
            [clojure.string :as s]))

(def search-terms ["I'm sad" "I am sad"
                   "i feel sad" "i’m feeling sad"
                   "you are sad" "we are sad"
                   "life sucks" "life is hard"
                   "this blows" "life is sad"
                   "not feeling it" "feeling blue"
                   "feelings suck" "today sucks"
                   "i suck" "this is so sad"
                   "that is the worst" "this is the worst"
                   "life is disappointing" "feeling gloomy"
                   "nothing matters"])

(def response-template "don't feel sad @%s :-) #FromSwedenWithLove")
(def response-template "don't feel sad @%s :-) #FromSwedenWithLove")
(def responses ["sending you hugs @%s! #FromSwedenWithLove"
                "we love you @%s! #FromSwedenWithLove"
                "always remenber Sweden loves you @%s x #FromSwedenWithLove"
                "Sweden is rooting for you @%s! #FromSwedenWithLove"])

(def creds (oauth/make-oauth-creds ""))

; create the client with a twitter.api streaming method and params of your choice
(def stream  (client/create-twitter-stream twitter.api.streaming/statuses-filter
                                           :oauth-creds creds :params {:track (s/join ", " search-terms)}))

(defn sub-str?
  [full-str sub-str]
  (s/includes?
    (s/lower-case full-str)
    (s/lower-case sub-str)))

(defn process-tweets
  [tweets]
  (doseq [tweet tweets]
    (when (some #(sub-str? tweet %) search-terms) 
       (rest/statuses-update-with-media :oauth-creds creds
                                        :params {:in_reply_to_status_id (:id tweet)}
                                        :body [(req/file-body-part "resources/testimage.gif")
                                               (req/status-body-part
                                                 (format (get responses (rand-int 4)) (get-in tweet [:user :screen_name])))]))
    (println (:text tweet))))

(defn main
  []
  (client/start-twitter-stream stream)

  (while true
    (process-tweets (:tweet (client/retrieve-queues stream)))
    (Thread/sleep (* 1024 5))))
