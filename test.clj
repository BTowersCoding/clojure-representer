#!/usr/bin/env bb

(require
 '[cheshire.core :as json]
 '[clojure.string :as str]
 '[clojure.java.shell :as shell]
 '[clojure.java.io :as io]
 '[babashka.fs :as fs])

(fs/unzip "clj-kondo")

(prn
  (fs/list-dir "resources/twofers/"))
