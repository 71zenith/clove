(ns clove.core
  (:require [clojure.string :as str])
  (:require [clj-http.client :as client])
  (:require [clojure.java.io :as io])
  (:use [hickory.core])
  (:import [java.util Base64])
  (:require [hickory.select :as s])
  (:gen-class))
(def user-agent "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0")
(def search-site "https://www.imdb.com/find")
(def base-url "https://vidsrc.me/embed/")
(def rcp-url "https://rcp.vidsrc.me/rcp")
(defn take-input
  "input"
  []
  (loop [input (read-line)]
    (if (empty? (str/trim input))
      (recur (read-line))
      input)))

(defn search
  "searches for material"
  [input]
  (->> input
       (str/trim)
       (#(client/get search-site {:headers {"User-Agent" user-agent} :query-params {"q" % "s" "tt" "ttype" ["tv" "ft"]}}))
       (:body)
       (parse)
       (as-hickory)
       (s/select (s/descendant (s/class "ipc-metadata-list-summary-item__t")))
       (map (fn [x] [(assoc {} :title (str/join(:content x)) :link (get-in x [:attrs :href]))]) )
       (flatten)
       ))

(defn hex-to-ascii [hex-str]
  (->> hex-str
       (partition 2)
       (map #(Integer/parseInt (apply str %) 16))
       (map int)
       (apply vector)))

(defn decode-base64
  "decodes base64"
  [hls]
  (->> hls
       (#(str/replace % "_" "/"))
       (#(str/replace % "-" "+"))
       (#(String. (.decode (Base64/getDecoder) (.getBytes % "UTF-8")) "UTF-8")))
  )
(defn recur-it
  "smtg"
  [b64]
  (let [input b64]
    (if (re-find #"\/@#@\/[^=\/]+==" input)
      (recur (str/replace input #"\/@#@\/[^=\/]+==" ""))
      input))
  )


(defn decode-hls-url
  "(final url)"
  [enc-url]
  (->> enc-url
       (#(subs % 2))
       (#(str/replace % #"/@#@/[^=\/]+==" ""))
       (recur-it)
       (decode-base64)
       ))

(defn decode-src
  [enc seed]
  "do magic"
  (let [enc-b (hex-to-ascii enc)
        seed-l (apply list seed)
        full (count enc-b)]
    (str/replace (str/join (map
                            #(char (bit-xor (get enc-b %)
                                            (int (nth seed-l (mod % (count seed-l))))))
                            (range full)))
                 #"^" "https:")
    ))

(defn get-source-url
  "returns source url"
  [url referer]
  (let [result (client/get url {:redirect-strategy :none :headers {"User-Agent" user-agent "Referer" referer}})]
    (->> result
         :headers
         :Location))
  )


(defn get-sources
  [vidsrc-url]
  "(hash referer)"
  (def get-hash (comp :data-hash :attrs))
  (let [result (client/get (str/join [base-url vidsrc-url]) {:headers {"User-Agent" user-agent}}) html (:body result) referer (first (:trace-redirects result))]
    (->> html
         (parse)
         (as-hickory)
         (s/select (s/descendant (s/class "server")))
         (first)
         get-hash
         (conj () referer)
         )))


(defn get-source
  [[hash referer]]
  "(url-referer source-url)"
  (def get-data-h (comp :data-h :attrs first #(s/select (s/id :hidden) %)))
  (def get-data-i (comp :data-i :attrs first #(s/select (s/tag :body) %)))
  (let [rcp-final (str rcp-url "/" hash) result (client/get rcp-final {:headers {"User-Agent" user-agent "Referer" referer}}) html (:body result)]
    (->> html
         (parse)
         (as-hickory)
         (#(decode-src (get-data-h %) (get-data-i %)))
         (list rcp-final)
         )))

(defn vidsrc-ex
  "final"
  [[referer url]]
  (def enc-hls-url (comp second #(re-find #"file:\"([^\"]+)\"" %)))
  (let [result (client/get url {:body "string" :headers {"User-Agent" user-agent "Referer" referer}})]
    (->> result
         :body
         (enc-hls-url)
         (decode-hls-url)
         ))
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [query (take-input)]
    (->> query
         (get-sources)
         (get-source)
         (vidsrc-ex))))
