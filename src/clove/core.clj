(ns clove.core
  (:require [clojure.string :as str])
  (:require [clj-http.client :as client])
  (:use [hickory.core])
  (:require [hickory.select :as s])
  (:gen-class))

(defn take-input
  "takes input from stdin"
  []
  (loop [input (read-line)]
    (if (empty? (str/trim input))
      (recur (read-line))
      input)))

(defn search
  "searches for material"
  [input]
  (let [query input agent "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0" site "https://www.imdb.com/find"]
    (->> query
         (str/trim)
         (#(client/get site {:headers {"User-Agent" agent} :query-params {"q" % "s" "tt" "ttype" ["tv" "ft"]}}))
         (:body)
         (parse)
         (as-hickory)
         (s/select (s/descendant (s/class "ipc-metadata-list-summary-item__t")))
         (#(map (fn [x] [(assoc {} :title (str/join(:content x)) :link (get-in x [:attrs :href]))]) %))
         )))

(defn get-sources
  []
  "return the hashes"
  (let [query "https://vidsrc.me/embed/tv/76479/3/1" agent "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0"]
    (->> query
         (#(client/get % {:headers {"User-Agent" agent}}))
         (:body)
         (parse)
         (as-hickory)
         (s/select (s/descendant (s/class "server")))
         (#(map (fn [x] [(assoc {} :source (str/join(:content x)) :hash (get-in x [:attrs :data-hash]))]) %))
         (flatten)
         )))

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
