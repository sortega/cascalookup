(defproject cascalookup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :aot [cascalookup.core]
  :main cascalookup.core.FindMismappings
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [cascalog "1.10.0"]
                 [clojure-csv/clojure-csv "2.0.0-alpha2"]]
  :dev-dependencies [[lein-marginalia "0.7.1"]]
  :profiles {:dev{:dependencies [[org.apache.hadoop/hadoop-core "0.20.2-dev"]]}})
