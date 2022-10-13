#!/usr/bin/env bb

(require '[babashka.fs :as fs])

(fs/unzip (fs/file (fs/cwd) "/main/clj-kondo"))

(prn (fs/list-dir (fs/path (fs/cwd) "/main/resources/twofers/")))
