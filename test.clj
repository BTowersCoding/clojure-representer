#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[clojure.java.shell :as sh])

(defn path-str [& parts] 
  (-> (apply fs/path parts) 
      fs/normalize
      str))

(fs/unzip 
  (fs/file
    (path-str "." "main") 
    "clj-kondo-2022.10.05-linux-static-amd64.zip")
    "/home/runner/.local/bin")

(fs/set-posix-file-permissions "/home/runner/.local/bin/clj-kondo" "rwxrwxrwx")

(let [paths (fs/list-dir (path-str "." "main" "resources" "twofers"))
      solutions (map #(slurp (fs/file % "two_fer.clj")) paths)
      unique (set solutions)]
    (println (str (count solutions) " total solutions"))
    (println (str (count unique) " unique solutions"))
    (run! 
     #(do 
       (sh/sh "bb" "main/clojure_representer.clj"
              "two-fer" (str %) (str %))
       (println "Source")
       (slurp (fs/file % "two_fer.clj"))
       (println "Representation")
       (slurp (fs/file % "representation.txt")))))
