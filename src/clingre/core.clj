(ns clingre.core
  (:require clj-http.client)
  (:require clojure.data.json)
  (:gen-class))

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
                 :room (clojure.string/join "," rooms)
                 :rooms rooms
                 :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (prn ['subscribe body])
    (when (= (body "status") "ok")
      (body "counter"))))

(defn observe [session counter]
  (let [json (clj-http.client/get
                       "http://lingr.com:8080/api/event/observe"
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
    (= "ok" (body "status"))))

(defn receive-loop [session events]
  (future
    (try
      (when-let [rooms (get-rooms session)]
        #_(prn ['session session 'rooms rooms])
        (loop [counter (subscribe-rooms session rooms)]
          (let [result (observe session counter)]
            (if-let [new-counter (result "counter")]
              (do
                (prn result)
                (swap! events conj result)
                (recur new-counter))
              (do
                #_(prn ['timedout result])
                (recur counter))))))
      (catch Exception e (str ['receive-loop-error e])))))

#_(defn prepare []
  (let [events (atom [])]
    (when-let [session (get-session)]
      (receive-loop session events)
      [events session])))

(defn -main [& args]
  (case args
    ["-h"] (prn 'help)
    (let [events (atom [])]
      (when-let [session (get-session)]
        (prn 'session session)
        (receive-loop session events)
        #_(loop []
          (print "\n> ")
          (flush)
          (let [input (try
                        (read-string (read-line))
                        (catch RuntimeException e nil))
                #_{:room "computer_science" :text "test from clingre 2"}]
            (let [{room :room text :text} input]
              #_(prn ['input input 'room room 'text text])
              (when (and room text)
                (let [body (say session room text)]
                  #_(prn (body "status"))
                  (print (format "\n%s" (body "status")))))))
          (recur))))
    #_(prn (say session "computer_science" "test from clingre"))
    #_(when-let [rooms (get-rooms session)]
      (prn 'session session 'rooms rooms)
      (loop [counter (subscribe-rooms session #_rooms ["vim" "simcity"])]
        (let [result (observe session counter)]
          (if-let [new-counter (result "counter")]
            (do (prn result) (recur new-counter))
            (recur counter)))))))
