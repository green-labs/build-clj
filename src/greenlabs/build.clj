(ns greenlabs.build
  "공통 빌드 스크립트.

  depstar, juxt/pack 대신 사용할 수 있습니다.
  tools.build를 기반으로 상위 태스크가 정의되어 있습니다.
  "
  (:require [clojure.tools.build.api :as b]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn repo-hash []
  (b/git-process {:git-args "rev-parse --short HEAD"}))

(defn repo-status []
  (b/git-process {:git-args "status --porcelain"}))

(defn clean
  "Clean a specific path

  Options:
    :path               - optional, defaults to 'target'"
  ([& {:keys [path] :or {path "target"}}]
   (b/delete {:path path})))

(defn uber
  "Create uberjar file

  Options:
    :uber-file          - required, uberjar file name (ex. 'backend.jar')
    :basis              - optional, used to pull dep jars, defaults to deps.edn basis
    :class-dir          - optional, local class dir to include, defaults to 'target/classes'
    :main               - optional, main class symbol"
  [{:keys [uber-file basis class-dir main]
    :or   {basis     (b/create-basis {:project "deps.edn"})
           class-dir "target/classes"}}]
  (assert uber-file "uber-file is required")
  (b/copy-dir {:src-dirs   ["src" "resources" "classes"]
               :target-dir class-dir})
  (b/compile-clj {:basis     basis
                  :src-dirs  ["src"]
                  :class-dir class-dir})
  (b/uber {:uber-file uber-file
           :basis     basis
           :class-dir class-dir
           :main      main})
  (println (str "Uber JAR created: \"" uber-file "\"")))

(defn uber-serverless
  "Create uberjar file for serverless project in '\"target/{{hash}}.jar\" path'.

  If a file with the same name exists, it is overwritten.
  If it's a git repository, no files should have changed since the HEAD commit.
  This must be run in root of the serverless project.
  'target/target.json' is created containing uberjar file name and version."
  []
  (assert (.exists (b/resolve-path "serverless.yml")) "You are not in a serverless project.")
  (clean)
  (let [commit-hash (repo-hash)
        uber-file (format "target/%s.jar" (or commit-hash "uber"))]
    (when commit-hash
      (assert (nil? (repo-status)) "The repository is not clean."))
    (b/write-file {:path   "target/target.json"
                   :string (json/write-str {:file    uber-file
                                            :version commit-hash}
                                           :escape-slash false)})
    (uber {:uber-file uber-file})))

(defn serverless
  "serverless 기본 설정(.yml) 파일을 생성합니다."
  [{:keys [serverless]}]
  (when (.exists (b/resolve-path "serverless.yml"))
    (println "serverless.yml already exists.")
    (System/exit 1))

  (let [;; temporary placeholder
        default-config {:service  "my-service"
                        :function "my-fn"
                        :file     "my-file"}
        config (merge default-config serverless)

        yaml (slurp (io/resource "serverless.yml"))
        output (reduce-kv
                 (fn [s k v]
                   (str/replace s (str "{{" (name k) "}}") v))
                 yaml
                 config)]
    (b/write-file {:path   "serverless.yml"
                   :string output})))
