(ns greenlabs.build
  "공통 빌드 스크립트.

  depstar, juxt/pack 대신 사용할 수 있습니다.
  tools.build를 기반으로 상위 태스크가 정의되어 있습니다.
  "
  (:require [clojure.tools.build.api :as b]
            [clojure.data.json :as json]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def commit-hash (-> (sh "git" "rev-parse" "--short" "HEAD")
                     :out
                     str/trim-newline))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s.jar" commit-hash))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/write-file {:path "target/target.json"
                 :string (json/write-str {:file uber-file :version commit-hash}
                                         :escape-slash false)})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis}))
