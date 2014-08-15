(ns clingre.core
  (:require [clj-http.client]
            [clojure.data.json]
            [clojure.main]
            [clojure.tools.reader :as r])
  (:gen-class))

(def ^:dynamic *debugging* true)

(defn- debug [x]
  (when *debugging*
    (prn x)))

(def clingre-key
  "oWBgRP"; clingr's
  #_"5xUaIa"; j6uil's
  )

(defn session-verify [session]
  (debug ['session-verify session])
  (let [json (clj-http.client/post
               "http://lingr.com/api/session/verify"
               {:form-params
                {:session session :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (= (body "status") "ok")))

(defn session-create []
  (debug 'session-create)
  (let [json (clj-http.client/post
               "http://lingr.com/api/session/create"
               {:form-params
                {:user "ujihisa@gmail.com"
                 :password "K3lx8t1"
                 :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (when (= (body "status") "ok")
      (body "session"))))

(defn get-rooms [session]
  (let [json (clj-http.client/get
               "http://lingr.com/api/user/get_rooms"
               {:query-params
                {:session session :app_key clingre-key}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (when (= (body "status") "ok")
      (body "rooms"))))

(defn subscribe-rooms [session rooms]
  (let [rooms (clojure.string/join "," rooms)
        json (clj-http.client/get
               "http://lingr.com/api/room/subscribe"
               {:query-params
                {:session session :rooms rooms}})
        body (try
               (clojure.data.json/read-str (:body json))
               (catch java.io.EOFException e {}))]
    (debug ['subscribe session rooms '=> body])
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
      body)))

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
        (debug ['session session 'rooms rooms])
        (loop [counter (subscribe-rooms session rooms)]
          (let [result (observe session counter)]
            (if-let [new-counter (result "counter")]
              (do
                (debug result)
                (swap! events conj result)
                (recur new-counter))
              (do
                (debug ['timedout result])
                (recur counter))))))
      (catch Exception e (str ['receive-loop-error e])))))

(defn start [filepath]
  (let [events (atom [])]
    (let [filedata (try
                     (binding [r/*read-eval* false]
                       (r/read-string (slurp filepath)))
                     (catch Exception e {}))
          session (let [session (and (map? filedata) (get filedata :session))]
                    (if (and session (session-verify session))
                      session
                      (when-let [session (session-create)]
                      (spit filepath (str (assoc filedata :session session)))
                      session)))]
      (if session
        (do
          (receive-loop session events)
          [session events])
        (prn "omg session-create failed")))))

(defn say-json [json]
  (let [dict (clojure.data.json/read-str json)]
    (if (map? dict)
      (if-let [{session "session" room "room" text "text"} dict]
        (say session room text)
        (binding [*out* *err*]
          (println "include session, room and text.")))
      (binding [*out* *err*]
        (println "json parse failed")))))

(defn
  ^{:doc "Starts repl within clingre.core namespace with fixed prompt."}
  start-repl []
  (clojure.main/repl :prompt #(print "\nCLINGRE=>")))

(defn -main [& args]
  (case args
    ["-h"] (prn 'help)
    (start "/home/ujihisa/.clingre.clj")
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
      (recur))
    #_(prn (say session "computer_science" "test from clingre"))
    #_(when-let [rooms (get-rooms session)]
      (prn 'session session 'rooms rooms)
      (loop [counter (subscribe-rooms session #_rooms ["vim" "simcity"])]
        (let [result (observe session counter)]
          (if-let [new-counter (result "counter")]
            (do (prn result) (recur new-counter))
            (recur counter)))))))
