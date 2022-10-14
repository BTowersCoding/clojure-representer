#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[clojure.java.shell :as sh])

(defn path-str [& parts] 
  (-> (apply fs/path parts) 
      fs/normalize
      str))

(fs/unzip 
  (fs/file "." "clj-kondo-2022.10.05-linux-static-amd64.zip")
    "/home/runner/.local/bin")

(fs/set-posix-file-permissions "/home/runner/.local/bin/clj-kondo" "rwxrwxrwx")

(let [paths (fs/list-dir (path-str "." "resources" "twofers"))
      solutions (map #(slurp (fs/file % "two_fer.clj")) paths)
      unique (set solutions)]
    (println (str (count solutions) " total solutions"))
    (println (str (count unique) " unique solutions"))
    (doseq [path (take 15 paths)]
      (let [representation (:out (sh/sh "bb" "clojure_representer.clj"
                           "two-fer" (str path) (str path)))]
        (println representation)
        (println "Source")
        (println (slurp (fs/file path "two_fer.clj")))
        (println "Representation")
        (println (slurp (fs/file path "representation.txt"))))))
