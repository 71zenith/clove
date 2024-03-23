(ns clove.core
  (:require [clojure.string :as str])
  (:require [clj-http.client :as client])
  (:require [clojure.java.io :as io])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:use [clojure.java.shell :only [sh]])
  (:use [hickory.core])
  (:import [java.util Base64])
  (:require [hickory.select :as s])
  (:gen-class))

(def user-agent "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0")
(def search-site "https://www.imdb.com/")
(def base-url "https://vidsrc.me/embed/")
(def rcp-url "https://rcp.vidsrc.me/rcp")

(defn take-input
  "input"
  [& prompt]
  (apply println prompt)
  (printf "=> ")
  (flush)
  (loop [input (read-line)]
    (if (empty? (str/trim input))
      (do
        (printf "=> ")
        (flush)
        (recur (read-line)))
      input)))

(defn ppp
  "imdb-id"
  [fmt-map]
  (let [lst (flatten (map keys fmt-map))
        fmt-list (str/join "\n" (map-indexed #(str (inc %1) ". " %2) lst))]
    (->>
     (take-input fmt-list)
     (read-string)
     (#(nth fmt-map (dec %1)))
     (vals)
     (apply str))))

(defn search
  "imdb-id"
  [input]
  (def shows (comp str/join :content))
  (def ids (comp #(re-find #"tt\d+" %) #(get-in % [:attrs :href])))
  (->> input
       (str/trim)
       (#(client/get (str/join [search-site "find/"]) {:cookie-policy :none :headers {"User-Agent" user-agent} :query-params {"q" % "s" "tt" "ttype" ["tv" "ft"]}}))
       (:body)
       (parse)
       (as-hickory)
       (s/select (s/descendant (s/class "ipc-metadata-list-summary-item__t")))
       (map (fn [x] (assoc {} (shows x) (ids x))))
       (take 8)
       (ppp)))

(defn filter-season
  "check if seasons"
  [m]
  (def check-s (comp #(get-in % [:attrs :data-testid])))
  (->> m
       (filter #(= "tab-season-entry" (check-s %)))))

(defn get-episode-list
  "get all episode"
  [imdb-id season]
  (def get-episode-num (comp #(str/replace % #"E" "") #(re-find #"E\d+" %)))
  (let [result (client/get (str/join [search-site "title/" imdb-id "/episodes"]) {:cookie-policy :none :query-params {"season" season} :headers {"User-Agent" user-agent}}) body (:body result) ]
    (->> body
         (parse)
         (as-hickory)
         (s/select (s/child (s/class "ipc-title__text")))
         (filter #(= (:tag %) :div))
         (map :content)
         (flatten)
         (map get-episode-num)
         (take-input "Eps:")
         (str "tv/" imdb-id "/" season "/"))))

(defn get-season-list
  "get season data"
  [imdb-id]
  (let [result (client/get (str/join [search-site "title/" imdb-id "/episodes"]) {:cookie-policy :none :headers {"User-Agent" user-agent} :throw-exceptions false})
        html (:body result)
        status (:status result)]
    (if (= status 404)
      (str "movie/" imdb-id)
      (->> html
           (parse)
           (as-hickory)
           (s/select (s/child (s/class "ipc-tab-link")))
           (filter-season)
           (map :content)
           (flatten)
           (take-input "Ss:")
           (get-episode-list imdb-id)))))

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
       (#(String. (.decode (Base64/getDecoder) (.getBytes % "UTF-8")) "UTF-8"))))

(defn recur-it
  "m3u8"
  [b64]
  (let [input b64]
    (if (re-find #"\/@#@\/[^=\/]+==" input)
      (recur (str/replace input #"\/@#@\/[^=\/]+==" ""))
      input)))

(defn decode-hls-url
  "(final url)"
  [enc-url]
  (->> enc-url
       (#(subs % 2))
       (recur-it)
       (decode-base64)))

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
                 #"^" "https:")))

(defn get-source-url
  "returns source url"
  [url referer]
  (let [result (client/get url {:cookie-policy :none :redirect-strategy :none :headers {"User-Agent" user-agent "Referer" referer}})]
    (->> result
         :headers
         :Location)))

(defn get-sources
  [vidsrc-url]
  "(hash referer)"
  (def get-hash (comp :data-hash :attrs))
  (let [result (client/get (str/join [base-url vidsrc-url]) {:headers {"User-Agent" user-agent}})
        html (:body result)
        referer (first (:trace-redirects result))]
    (->> html
         (parse)
         (as-hickory)
         (s/select (s/descendant (s/class "server")))
         (first)
         get-hash
         (conj () referer))))

(defn get-source
  [[hash referer]]
  "(url-referer source-url)"
  (def get-data-h (comp :data-h :attrs first #(s/select (s/id :hidden) %)))
  (def get-data-i (comp :data-i :attrs first #(s/select (s/tag :body) %)))
  (let [rcp-final (str rcp-url "/" hash)
        result (client/get rcp-final {:headers {"User-Agent" user-agent "Referer" referer}})
        html (:body result)]
    (->> html
         (parse)
         (as-hickory)
         (#(decode-src (get-data-h %) (get-data-i %)))
         (list rcp-final))))

(defn vidsrc-ex
  "link"
  [[referer url]]
  (def enc-hls-url (comp second #(re-find #"file:\"([^\"]+)\"" %)))
  (let [result (client/get url {:body "string" :headers {"User-Agent" user-agent "Referer" referer}})]
    (->> result
         :body
         (enc-hls-url)
         (decode-hls-url))))

(def cli-options
  [["-p" "--player <program>" "Media Player"
    :default "mpv"
    ]
   ["-d" "--debug" "Raw link"]])

(defn play
  [opts link]
  (condp = (nil? (:debug opts))
     false (println link)
     true (sh (:player opts) link)))

(defn -main
  "I do a whole lot actually..."
  [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-options)
        query (str/join " " arguments)]
    (try
      (->> query
           (search)
           (get-season-list)
           (get-sources)
           (get-source)
           (vidsrc-ex)
           (play options))
      (System/exit 0)
      (catch Exception e (println (str "caught exception: " (.getMessage e)))))))
