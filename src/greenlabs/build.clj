(ns greenlabs.build
  "공통 빌드 스크립트.

  depstar, juxt/pack 대신 사용할 수 있습니다.
  tools.build를 기반으로 상위 태스크가 정의되어 있습니다.
  "
  (:require [clojure.tools.build.api :as b]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]))

(def commit-hash (-> (sh "git" "rev-parse" "--short" "HEAD")
                     :out
                     str/trim-newline))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s.jar" commit-hash))

;; 람다 실행을 위한 최소한의 권한이 부여된 IAM Role 입니다.
(def aws-role "arn:aws:iam::887960154422:role/lambda-exec-role")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
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
           :basis     basis}))

(defn serverless
  "serverless 기본 설정을 생성합니다.

  config 에는 템플릿 변수에 해당하는 key가 있어야 합니다.
  "
  [{:keys [serverless]}]
  (when (.exists (b/resolve-path "serverless.yml"))
    (println "serverless.yml already exists.")
    (System/exit 1))

  (let [;; temporary placeholder
        default-config {:service  "my-service"
                        :function "my-fn"
                        :file     "my-file"
                        :role     aws-role}
        config         (merge default-config serverless)

        yaml           (slurp (io/resource "serverless.yml"))
        output         (reduce-kv
                         (fn [s k v]
                           (str/replace s (str "{{" (name k) "}}") v))
                         yaml
                         config)]
    (b/write-file {:path   "serverless.yml"
                   :string output})))
