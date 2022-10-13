#!/usr/bin/env bb

(require '[babashka.fs :as fs])

(defn path-str [& parts] 
  (-> (apply fs/path parts) 
      fs/normalize
      str))

(fs/unzip 
  (path-str "." "main") 
    "clj-kondo"))

(prn 
  (fs/list-dir 
    (path-str "." "main" "resources" "twofers")))
