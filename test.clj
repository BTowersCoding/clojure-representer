#!/usr/bin/env bb

(require '[babashka.fs :as fs])

(defn path-str [& parts] 
  (-> (apply fs/path parts) 
      fs/normalize
      str))

(fs/unzip 
  (fs/file
  (path-str "." "main") 
    "clj-kondo-2022.10.05-linux-static-amd64.zip"))

(-> (path-str "." "main" "resources" "twofers")
    fs/list-dir 
    first
    (fs/file "two_fer.clj")
    slurp
    prn)
