#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[cheshire.core :as json]
         '[clojure.java.shell :as shell]
         '[clojure.string :as str]
         '[clojure.edn :as edn]
         '[rewrite-clj.zip :as z])

(def slug (first *command-line-args*))
(def in-dir (str (fs/path (second *command-line-args*))))
(def out-dir (str (fs/path (last *command-line-args*))))

(comment
  (def slug "two-fer")
  (def in-dir "resources/twofers/49")
  (def out-dir "resources/twofers/49")
  )

(defn snake-case [kebab-case]
  (str/replace kebab-case "-" "_"))

(def analysis
  (let [cmd ["clj-kondo" "--lint" (str (fs/file in-dir (str (snake-case slug) ".clj"))) "--config"
             "{:output {:format :edn},:analysis {:locals true :arglists true}}"]]
    (:analysis (edn/read-string (:out (apply shell/sh cmd))))))

(def locals
  (let [rows (->> analysis
                  :locals
                  (into (:local-usages analysis))
                  (map #(select-keys % [:name :row :col]))
                  (filter :name)
                  (group-by :row)
                  (map last))
        cols (map #(sort-by :col %) rows)]
    (flatten (sort-by #(:row (first %)) cols))))

(def placeholders
  (let [args (->> analysis
                  :var-definitions
                  (mapcat :arglist-strs)
                  (mapcat edn/read-string)
                  (map str)
                  set)
        locals (map str (map :name (:local-usages analysis)))
        placeholders (map #(str "PLACEHOLDER-" %) 
                          (range 1 (inc (+ (count locals) (count args)))))]
    (println (str "Generating " (fs/file out-dir "mapping.json")))
    (spit (fs/file out-dir "mapping.json")
          (json/generate-string 
           (into (sorted-map-by 
                  (fn [key1 key2]
                    (< (parse-long (last (str/split key1 #"-")))
                       (parse-long (last (str/split key2 #"-"))))))
                 (zipmap placeholders (into args locals)))
           {:pretty true}))
    (println (slurp (fs/file out-dir "mapping.json")))
    (zipmap (into args locals) placeholders)))

(def impl
  (z/of-file (str (fs/file in-dir (str (snake-case slug) ".clj")))
             {:track-position? true}))

(defn replace-local [z {:keys [name row col]}]
  (z/replace (z/find-last-by-pos z [row col]) 
             (get placeholders (str name))))

(defn arglists [var-def]
  (let [lists  (:arglist-strs var-def)]
    (into [] (for [list lists]
               {:var  (symbol (:name var-def))
                :args (mapv symbol (edn/read-string list))}))))

(defn replace-arglist [z arglist]
  (-> z
      (z/find-value z/next (:var arglist))
      (z/find-next-depth-first #(= (:args arglist) (z/sexpr %)))
      (z/replace (mapv #(get placeholders (symbol %))
                       (:args arglist)))
      z/root-string
      (z/of-string {:track-position? true})))

(defn replace-locals [zloc]
  (loop [z zloc idents locals]
    (if (empty? idents) (println (z/root-string z))
        (recur (z/of-string (z/root-string (replace-local z (last idents)))
                            {:track-position? true})
               (butlast idents)))))

(defn represent [zloc]
  (spit (fs/file out-dir "representation.txt") 
    (with-out-str (-> (reduce replace-arglist zloc
                              (mapcat arglists (:var-definitions analysis)))
                      replace-locals
                      z/root-string))))

(let [mapping (str (fs/file out-dir "mapping.json"))]
  (println (represent impl))
  (println mapping)
  (println (slurp mapping))
  (System/exit 0))
