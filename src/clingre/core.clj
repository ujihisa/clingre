(ns clingre.core
  (:require clj-http.client)
  (:require clojure.data.json))

(def clingre-key "oWBgRP")

(defn get-session []
  (let [json (clj-http.client/post
               "http://lingr.com/api/session/create"
               {:form-params
                {:user "ujihisa@gmail.com"
                 :password ""
                 :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (when (= (body "status") "ok")
      (body "session"))
    #_(when (= (-> json :body :status) "ok")
      (prn 'cool))))

(defn get-rooms [session]
  (let [json (clj-http.client/get
               "http://lingr.com/api/user/get_rooms"
               {:query-params
                {:session session
                 :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (when (= (body "status") "ok")
      (body "rooms"))
    #_(when (= (-> json :body :status) "ok")
      (prn 'cool))))

(defn subscribe-rooms [session rooms]
  (let [json (clj-http.client/post
               "http://lingr.com/api/room/subscribe"
               {:form-params
                {:session session
                 :rooms rooms
                 :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (when (= (body "status") "ok")
      (body "counter"))))

(defn observe [session counter]
  (let [json (clj-http.client/get
                       "http://lingr.com/api/event/observe"
                       {:query-params
                        {:session session
                         :counter counter
                         :app_key clingre-key}})
                body (try
                       (clojure.data.json/read-str (:body json))
                       (catch java.io.EOFException e {}))]
    (when (= (body "status") "ok")
      body)
    #_(when (= (-> json :body :status) "ok")
      (prn 'cool))))

(defn say [session room text]
  (let [json (clj-http.client/post
               "http://lingr.com/api/room/say"
               {:form-params
                {:session session
                 :room room
                 :text text
                 :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    body))

(defn -main [& args]
  (case args
    ["-h"] (prn 'help)
    (when-let [session (get-session)]
      (future
        (when-let [rooms (get-rooms session)]
          (prn ['session session 'rooms rooms])
          (loop [counter (subscribe-rooms session rooms #_["computer_science" "vim"])]
            (let [result (observe session counter)]
              (if-let [new-counter (result "counter")]
                (do (prn result) (recur new-counter))
                (recur counter))))))
      (loop []
        (let [input (try
                      (read-string (read-line))
                      (catch RuntimeException e nil))
              #_{:room "computer_science" :text "test from clingre 2"}]
          (let [{room :room text :text} input]
            (prn ['input input 'room room 'text text])
            (when (and room text)
              (let [body (say session room text)]
                (prn (body "status"))))))
        (recur)))))
