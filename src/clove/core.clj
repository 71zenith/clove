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
(def base-url "https://vidsrc.me")
(def enc-url "#9aHR0cHM6Ly90bWRydi52aWRzcmMuc3RyZWFtL3N0cmVhbS9INHNJQUFBQUFBQUFBdzNPMFc2RElCUUEwRjhDMGJZMDZjTmNLMnN6eUVTNEtHOElycTVxWTR4cj/@#@/JT82NDk3Lls6NA==FLX2Z6aGNjakdKQ0NOMDNCNG9URjNDeXB5NG1IbjM3MmljSDdJNzJFVGlI/@#@/MzMtKi/@#@/Kiw0KS4oXykoKQ==40LzlbNg==ZGxVcnZlYWRSSHJyTDFKbmtVSzA1RU40V1NSdUJlS0w2R0FTUTc3S00ueThTZ3N/@#@/Ol0mKjFAQDE9Jg==lSmJwNGVseXJmdnpjTkRIZ1h6QmtFXy5Rek9pWU5KQnk5MnhfYW9hdDNlNExzTG5rYk93Z3luWW1hMW1sYnFsR/@#@/PSg9OjE5NzA1Lw==lkxa1Y4N3ZRSU9zb0dFZHNxdEVjQ1NKLks1eWp3T2pEcXJBMDVfc0VuWjNWZ0hIbzJyYTRKTExxMzdBdzlPcjZEUDdQNlpkQ3B6X3EuUkNyN1FBQUFBLS0vbWFzdGVyLm0zdTg=")
(def test-hash "YjEzM2Y2YTZjYjIxMzg1NThhZTRmNmQ1ZDVlMmQ1MTU6WlhwdE0ycFBLMjVDY1ZKYVNqbHJaMjlFVldwaVFWZzVUR0Y1WVhkdk5VeG9LemROVDJSaGVDOVhMMHg0VGxwMGNFRk1PVVZhU3paQlVtNDBXbXd5YmtoTVJVTnZWWEZYY2pBM1JsaEJZVXRNWVhaa1drbFRZblpDTTB0eVFtNUNkMVkzVUN0RFJsTnFaMjlxVkRscWFUZE9aVUoxUVVOV2IyMVNSSGR2Y21seFpXZFhiV1ZGYTFSUVRVOUhkRkZIZEdGNWJqTjFPWE5MVjBaVE5YZ3dOMWhpV1hOemRGQjBNaTl4WmxBMGJXeFZNbkJOVGtKSFNtMVFMMGhaV0VGTVJsaG5VR2h5VTBzMmFrNVFVRVJSUTJsU1RUVTJNMkp0TURsV1R6TTNVMWMzTlRWS2VEWk1ha1JxTVhseFZuaG9UbUV4TlRkelQydzJlbEUyWm5KRVowSk1TR1kwU0daaVFqVnljM01yYVZkSFJsaG9OSEZQY21WQ09HTnZUMWQyWjNaNU1saFROR05pV0VsdFVFeEZhbGh3TDJwRlJHVnZiazF2VFRob2EwMHlXRzEwUkc5WVUwVTRSUzkwTkhnMU0zWnpUeTl0ZGk5UVExQk1VVmhSWVdaS1VITkROV1ptY1dGRU9ISlJXRUpaWlhGWmMwaGxTRVJXV0c1emNtb3dSV3cyWWpWRU1scFRObm9yY0hGUlJXbFFhRGR1TlV0MFFWbG1iVmhzZEM5UWVYVkliRFJxUkZFd1dsSlZiVWRDT0V4RmNtOXZXa1pDV0dGNmR5dHpTMjlKYjJWMFFXbE5ValpQZFN0RVVreGFlRFJaVVdJNFVsbE9jV1pGUVZGVFdqbDBUMnRwSzFkdUt5dE9XRXBtWW5VeFlVNUdibEZoYldkM2VXcHdOM0J3UW5aUmFVZHBXR1ZCYVdKdFNFRnlTMnhzUlZaS1ZUUktVVE5XVG5sYWRuWXhla1kwUjJGMFp6WXZUMWQxTUZabVZETnNSWFJoZFROSGJWWnJOa0ZoYXk5a05VOWpVSGRyU0djeWJtdDVVMWRQTVZkck1rNTRaMjVPTlVzd05FRnpRbWRGWmxaNE5FUm1jWEZyUlhGS1NsTmhhMGhFZWs5UVExZEZjSEZoY2tGdE1rVTBTMVJRYVRsR01qWmxiME5RYVRSdGFITkJlbWczZVVkaWVHVmhOSGt6YlU1U2FFRjBZV2xGWkhaeWRFMXRjRk5ETjJZeUwyNWFWMk5QYXpjPQ--")
(def test-ref "1e1e4f595240463c1d15541e424b53445044707e025d5d68437d057e70195f350377586064745a6d0b611268036355695c7a05055921497e76600178746169692e52007c0b7a61527009662e6659616c66606479335709605f635251045b4c0601006252644a4762677e11512c7760676e015d52623b6b2b5d7f586f5b7f4b6033572b675c7c405470435e09664d7e675a5d7f55657a18612c77667c6e084e66062c022f7404796c5d4e6b556f432d665c456b7d5d444d0c5d0850537463426c7440307d2d0548527e485e7e5927072c00597a740045496e197528675a5e0b655b02043e751c457e757b6854586e2e652c737e527e66606a4e1d61355c08056375605c621958026b79416f7d7e716006673a7b545d776861585c0765146b5e6453727c7d716a752f5d41696f675e796017433b50677f617e6e79580d652d4966657f5b657571266014776b50525c4f6a6019611d777f68604a60696225652b685c4172647043610c673a5a7c756f516066796a503f7767527c6a5d65616a522c5b73666c0264476132471263007b726673696e125e2a7853777f7f64010c6f6a2f0104627e6271625a33592e6401016d656c58671957307c640876660561430964490155006b7678655c127e3e41646b7d7a615172056b36695d50585d410163080217645d677e6a737d611275087265016b736274662c6015046467527a026572097d1d5d77076f5c527c56336919650064095560616d0c5832647c795167605d5f6a643f7b4562526a575207124b2d4b777b5c75074161335b14505c597a665c79730b7508646b767f7f7b74660d65127376530862607c61336635015e006065527c5132652055747f527d06695c0664005063776b4279777e0c7e1545056754447165720e4b190241506f5b466a6d3579025501044d557e7d610d662e05624b7351527465287c20636b7c6e58406a04055c2c0100795d75785879337933675d554e515b65723c000867630278496265576a7e3e7b7652714064525f1560215d677e58666c47566c692c6365670c6958437f115e2a69625c4d5c547b7615653e5249677e7e4457716e642e747b446d5d466455327a0f7c64556a52606a060577367e7c006f65675d5034512c704b66540579526211411976775058035e4b6d6c43167c5d4170625871551264360565664d7b615f50177d2f08596761624e65623b4536667f686a5c7c69620a654b6467670d635a7d4508673e5853766b766358796a7d2e7c487f616a66664e33002276415e5a766749613575116703637c6905435f11764940665f73647b026e136613557e536f764257723b5a2a0363596f76526579340209645b7753515b4b050a652272645c7c48635d5c0c603f5249686c667d607215662d5f557c745e647b6e1a6137627763706558436408741b4668646f4a5203012b612c735050554059656016071b5d55436e5e5a7c660b5f02670155637d0761460a58227d7c034d0462666e2c66145a496557626e650515702e6649766c02784167346512677659496a7365040d642e405374515864644c6965496b5e53520960577219642a0309016e5a7840626e65216469555267714b4706022e4667007745625e0108603d000766526602600609472d5d636674787b496d324b0f6476777e615d476109001c0566657801525e7218503e735863547d41645f37581d5a08775a75035f66346a016466497664706243085e3e62676773016f655c28604a7707507f7a795173384a2276454b77667b4a571a472250027f0c6906695f3d5e32437f64557e536b666c693c7766650b7a7351711158195a04796f006469640e1e55")
(def test-rcp "https://rcp.vidsrc.me/rcp/YjEzM2Y2YTZjYjIxMzg1NThhZTRmNmQ1ZDVlMmQ1MTU6WlhwdE0ycFBLMjVDY1ZKYVNqbHJaMjlFVldwaVFWZzVUR0Y1WVhkdk5VeG9LemROVDJSaGVDOVhMMHg0VGxwMGNFRk1PVVZhU3paQlVtNDBXbXd5YmtoTVJVTnZWWEZYY2pBM1JsaEJZVXRNWVhaa1drbFRZblpDTTB0eVFtNUNkMVkzVUN0RFJsTnFaMjlxVkRscWFUZE9aVUoxUVVOV2IyMVNSSGR2Y21seFpXZFhiV1ZGYTFSUVRVOUhkRkZIZEdGNWJqTjFPWE5MVjBaVE5YZ3dOMWhpV1hOemRGQjBNaTl4WmxBMGJXeFZNbkJOVGtKSFNtMVFMMGhaV0VGTVJsaG5VR2h5VTBzMmFrNVFVRVJSUTJsU1RUVTJNMkp0TURsV1R6TTNVMWMzTlRWS2VEWk1ha1JxTVhseFZuaG9UbUV4TlRkelQydzJlbEUyWm5KRVowSk1TR1kwU0daaVFqVnljM01yYVZkSFJsaG9OSEZQY21WQ09HTnZUMWQyWjNaNU1saFROR05pV0VsdFVFeEZhbGh3TDJwRlJHVnZiazF2VFRob2EwMHlXRzEwUkc5WVUwVTRSUzkwTkhnMU0zWnpUeTl0ZGk5UVExQk1VVmhSWVdaS1VITkROV1ptY1dGRU9ISlJXRUpaWlhGWmMwaGxTRVJXV0c1emNtb3dSV3cyWWpWRU1scFRObm9yY0hGUlJXbFFhRGR1TlV0MFFWbG1iVmhzZEM5UWVYVkliRFJxUkZFd1dsSlZiVWRDT0V4RmNtOXZXa1pDV0dGNmR5dHpTMjlKYjJWMFFXbE5ValpQZFN0RVVreGFlRFJaVVdJNFVsbE9jV1pGUVZGVFdqbDBUMnRwSzFkdUt5dE9XRXBtWW5VeFlVNUdibEZoYldkM2VXcHdOM0J3UW5aUmFVZHBXR1ZCYVdKdFNFRnlTMnhzUlZaS1ZUUktVVE5XVG5sYWRuWXhla1kwUjJGMFp6WXZUMWQxTUZabVZETnNSWFJoZFROSGJWWnJOa0ZoYXk5a05VOWpVSGRyU0djeWJtdDVVMWRQTVZkck1rNTRaMjVPTlVzd05FRnpRbWRGWmxaNE5FUm1jWEZyUlhGS1NsTmhhMGhFZWs5UVExZEZjSEZoY2tGdE1rVTBTMVJRYVRsR01qWmxiME5RYVRSdGFITkJlbWczZVVkaWVHVmhOSGt6YlU1U2FFRjBZV2xGWkhaeWRFMXRjRk5ETjJZeUwyNWFWMk5QYXpjPQ--")
(def test-vid "https://vidsrc.me/srcrcp/Nzk5OGZkYjcxZmM2MDkzN2QyYjgxZjM0OGYwYmY1ODU6YkZkS1NHMXZSbk5HWlhBM1l5OXJha0ZKTVZkdGVWZHdkM1pMTVV4R1kweHFObGxsYVRKTWEyZHJiWFl5YW1GMU5qazVjSFJxTWxCck5VdGFWWHBrTW1nNU1XMVdLMFozZVZZd2REbGpTR1pJWTFjMmRHVXlObUk0TTNWclVUWkhhMVJPYTNoT1NtOHZXV1paVnpkVVZXbDRXbVZ4ZUd4c2RXbDJOVEZPZURkR2VVTnZXbWNyY1hwcFJrMU5UamhZY1dOV2NWZHJkbkJFY21WbWFEWTJjRTV2WlZWb1QzSlZMMDB6TUdaRFIyeHRNRlZUVFVReU5rNXJhMEYxYTNkWE1FeHRNbHBpY0ZJd1FXbGxlRWh3YlRWaVUyZFRNak5qVldWelZVVkJXbFJYZGt4V1owbHlPRTlOVWk5RGJuZHFSQ3M0ZFRsUlMzcEpkbU16TkhORFoxUjNiWEZqYTNsa2VrOXBjVUZLVTBsc2VHdGtaR0ZtYVVsM1VGTkVVbVJTZEdadmFtZDFZVTgzZFZkQmIxSXdlR1JZVUdsSlR6bGhkRlpxVW1WeFdrUkdlRVU1TlRKMFJYUkhkbmhhYTJ0dU1ucGxTMEpGZFV4eFIxVTBWWGRDTVdJeVNHbzRNV1ZRS3pGT2NtWjNWSHBQVDI1a1JVbDJhMGhwTUdoMkx6SnRjWFp1V1VGc2VqSnBNamQwWjBsYWVVZDBVek5oYURKU2JWWlRhSFZRTjFSaVYwcHBWRloxYTNKNWJGQnpWbmR2VVVoQk5tTmhVbVpJT0Znd1dHTlRWa3RDZG1OR1oyRjFUbEJtUVc1d0swUk1TM1ZtVG5kc2JqVkNUSFZLV0dOT1IzRXJNVE13VkZJcmRrcDZaR3hGV21wNVdrNWtUek4zZFVZdlUwNU5TalJNTnpGMFdWZFVaM2xoYVV4WWR6QlRUbWh4UzB0SVVtTm5hM0pUY0ZWcFRtVTNWV0Y2U0RJeWNVOVlhSEZsWTFSWGVHOXhXbUUzY2pFMWNrNU5TamcyT0VGRU1rUk5NV1JaYjAwM2VucFFVMmgwYTJoUlNsQm9kRFpVY2pGbVFYTjZUM2Q1YVZORGJGUlhUR2MzTTNwb2NYRkNVazVYY0ZvMGFXTlliWGxMY25kamFtUjVlV2xRVm1OdFEzaFhiMDQxWmpCTVFURkVTeklyZFZOUldFWnNNelZtYms1NFZpOHhXSGhMTDI5Q2VIZ3djMDlRWVRCUGFEWnlOWGw2V2tZPQ--")
(def test-seed "1190634_3x1")
(def referer "https://vidsrc.xyz/")
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
       (map (fn [x] [(assoc {} :title (str/join(:content x)) :link (get-in x [:attrs :href]))]) )
       (flatten)
       ))

(defn hex-to-ascii [hex-str]
  (->> hex-str
       (partition 2)
       (map #(Integer/parseInt (apply str %) 16))
       (map int)
       (apply vector)))

(defn decode-hls-url
  "decodes hls url"
  [enc-url]
  (->> enc-url
       (#(subs % 2))
       (#(str/replace % #"/@#@/[^=\/]+==" ""))
       (recur-it)
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

(defn get-sources
  [vidsrc-url]
  "(hash referer)"
  (def get-hash (comp :data-hash :attrs))
  (let [result (client/get vidsrc-url {:headers {"User-Agent" user-agent}}) html (:body result) referer (first (:trace-redirects result))]
    (->> html
         (parse)
         (as-hickory)
         (s/select (s/descendant (s/class "server")))
         (first)
         get-hash
         (conj () referer)
         )))


(defn get-source
  [hash referer]
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
         ))
)

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
