(ns greenlabs.build
  "공통 빌드 스크립트.

  depstar, juxt/pack 대신 사용할 수 있습니다.
  tools.build를 기반으로 상위 태스크가 정의되어 있습니다.
  "
  (:require [clojure.tools.build.api :as b]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn repo-hash []
  (b/git-process {:git-args "rev-parse --short HEAD"}))

(defn repo-status []
  (b/git-process {:git-args "status --porcelain"}))

(defn uber
  "\"target/{{file-name}}.jar\" 경로에 uberjar를 생성합니다.
  같은 이름의 파일이 있다면 덮어씌워집니다.
  만약 git 저장소라면 HEAD 커밋 이후로 변경된 파일이 없어야 합니다.

  Options:
    :file-name - optional, 생성될 uberjar의 이름입니다. default 값은 커밋 hash 입니다."
  [{:keys [file-name]}]
  (clean nil)
  (let [commit-hash (repo-hash)
        uber-file   (format "target/%s.jar" (or file-name commit-hash "uber"))]

    (when commit-hash
      (assert (nil? (repo-status)) "The repository is not clean."))

    (b/write-file {:path   "target/target.json"
                   :string (json/write-str {:file uber-file :version commit-hash}
                                           :escape-slash false)})
    (b/copy-dir {:src-dirs   ["src" "resources"]
                 :target-dir class-dir})
    (b/compile-clj {:basis     basis
                    :src-dirs  ["src"]
                    :class-dir class-dir})
    (b/uber {:class-dir class-dir
             :uber-file uber-file
             :basis     basis})

    (println (str "Uber JAR created: \"" uber-file "\""))))

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
        config         (merge default-config serverless)

        yaml           (slurp (io/resource "serverless.yml"))
        output         (reduce-kv
                         (fn [s k v]
                           (str/replace s (str "{{" (name k) "}}") v))
                         yaml
                         config)]
    (b/write-file {:path   "serverless.yml"
                   :string output})))
