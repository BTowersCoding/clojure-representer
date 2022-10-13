#!/usr/bin/env bb

(require '[babashka.fs :as fs])

(defn path-str [& parts] 
  (-> (apply fs/path parts) 
      fs/normalize
      str))

(fs/unzip 
  (fs/file
  (path-str "." "main") 
    "clj-kondo-2022.10.05-linux-static-amd64.zip")))

(prn 
  (fs/list-dir 
    (path-str "." "main" "resources" "twofers")))
