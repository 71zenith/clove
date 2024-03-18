(ns clove.core
  (:require [clojure.string :as str])
  (:require [clj-http.client :as client])
  (:use [hickory.core])
  (:require [hickory.select :as s])
  (:gen-class))
(def user-agent "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0")
(def search-site "https://www.imdb.com/find")
(def base-url "https://vidsrc.me")
(def test-hash "ODgyMzI5Y2QzMDM1NmFlZjk2MzQ2NGJmZjJjNDQ3MWE6VWpWWmVWZGlSRzlqWmtaT09UaFNORUppUzBaaGRFOWtUSEJxTVhCUE1XZFhaRE5QT0UxNVVHOUtjR28zWkVGeWFXTTFLMk5EUXpSMVFqbG9UM2cyTjBrMllVbENkRTE2VkRKeGRrMUJTbWhhVUdwbmN6WTJURk0xWTA5RE5GWjBlVE5XYUhOdFRWVTBOa1ZLV2pGNlVrVlNkbk42VHpkQmNtOUpZMDVCYmtWdFZUSkxSMGRCU2sxdFRubHFkMGgwZGpaSmJHbDNaa1psUWs1dFIxUk9lbWxKWlVGM05HWnVVREIzVjNSMGQwMXZXbUZEWm5Kb2REVk9TRGN2TjB3ck5UaElkazl5WjFCV2VuWXlWVWNyTVdwd1FXRlNWM0YxYjI5b1VGWnlZV1pIZFRCdE5HaFZWWEpFUTNGcVVuUnBka2x1UmtONldISjZZMU5HZVc5eFFXa3ZNakpTYkhWVlUydG1PVmRrY1RseVlXcDBNRlpYTlUxcGVFWlpUMFpEWWpsUE1Ha3laRTR4Ulc1SkswUTRlbFpRVDFoQlRWVlZVa3BtTld4MVNqRlBaakUyWXpWc1lXSlFSMlptV2pOa04yZDFXVmRXY21OUmEyTTJPSFZMY1cweFVuUldPR1oyYmtwSGNIQlFhak13VmxVd1RsbzRRbmhwVnpod1ZXb3lVRk41ZWxJM1FrNVNaRzV1WnpoTVdWaGhMMFp6TUdGUlQxZDBUV3hEWVRKcmNuTTRlVWt6VTI1VFIxcDJhMFJIYVhGNlprcDJiMGt2TWsweVlqSnJialJUYkhSclZtcEJkMjFJTDFGblZGWTJNbTExUkVoR2JFWjVOMmQxV0ZWclNYUlBaRXM0VG1WRUszazBNRk0yTW5wM2F6TjJUakJuWlZOR09HWnZNU3Q0WkRac1JuRnpla2RQV0VFd09IUkJaSEpHWVhOUVJuUnZZMHN5VTJOc2RFbHVjRW92UmxSS1lXaEdWbk5qVVc1b1FsWTRTV3hQYldGamVqSXJiekpHVWtwT2FsTk1ZVE5tWjBKSFlVSjBNR0pLYVZScVRra3hXbkJ2TkVsSGEzQklNRTh6YWpsNFFqUkhOVGhsWTA1a1JFUTRWVmRuWTBONlpEbGxaVzF4ZUZaeVVqTnFabE5UVEVSdVdFdEtSM000UVdGalkzTkJWRlZyTUdWMFVITjJVbEZyZGtWamNTOXFiamR4TVZBNVV6ZFJiWE0yVEVweGIzZDJaakJXVW13PQ--")
(def referrer "https://vidsrc.xyz/")
(def rcp-url "https://rcp.vidsrc.me/rcp")
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
  (->> input
       (str/trim)
       (#(client/get search-site {:headers {"User-Agent" user-agent} :query-params {"q" % "s" "tt" "ttype" ["tv" "ft"]}}))
       (:body)
       (parse)
       (as-hickory)
       (s/select (s/descendant (s/class "ipc-metadata-list-summary-item__t")))
       (#(map (fn [x] [(assoc {} :title (str/join(:content x)) :link (get-in x [:attrs :href]))]) %))
       (flatten)
       ))

(defn get-sources
  []
  "return the hashes and referrer"
  (let [result (client/get "https://vidsrc.me/embed/tv/76479/3/1" {:headers {"User-Agent" user-agent}}) html (:body result) referrer (first (:trace-redirects result))]
    (->> html
         (parse)
         (as-hickory)
         (s/select (s/descendant (s/class "server")))
         (#(map (fn [x] [(assoc {} :source (str/join(:content x)) :hash (get-in x [:attrs :data-hash]))]) %))
         (flatten)
         (#(conj % referrer))
         )))

(defn get-source
  [hash referrer]
  "return url and url-referrer"
  (def get-data-h (partial s/select (s/id :hidden)))
  ;; (def get-data-i (partial s/select (s/tag :data-i)))
  (let [result (client/get (str rcp-url "/" hash) {:headers {"User-Agent" user-agent "Referer" referrer}}) html (:body result)]
    (->> html
         (parse)
         (as-hickory)
         (get-data-h)
         (#(map (fn [x] (get-in x [:attrs :data-h])) %)))
      ))
(defn -main
  "I don't do a whole lot ... yet."
  [& args])
