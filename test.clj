#!/usr/bin/env bb

(require
 '[cheshire.core :as json]
 '[clojure.string :as str]
 '[clojure.java.shell :as shell]
 '[clojure.java.io :as io]
 '[babashka.fs :as fs])

(prn (fs/cwd))

(fs/unzip "main/clj-kondo")

(prn (fs/list-dir "main/resources/twofers/"))
