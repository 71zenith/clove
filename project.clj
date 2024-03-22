(defproject clove "0.2.0"
  :description "vidsrc scraper"
  :url "https://github.com/71zenith/clove"
  :license {:name "GPL-3.0"
            :url "https://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [org.clojure/tools.cli "1.1.230"]
                 [hickory "0.7.1"]]
  :main ^:skip-aot clove.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
