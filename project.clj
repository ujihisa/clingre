(defproject clingre "1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/ujihisa/clingre"
  :license {:name "GPL-3+"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.2"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/tools.reader "0.8.5"]]
  :main ^:skip-aot clingre.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
