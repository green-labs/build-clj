# build-clj

tools.build 사용하듯이 `:build` 별칭으로 사용할 수 있습니다.
 
```clojure
:aliases {:build {:deps {io.github.green-labs/build-clj
                         {:git/tag "v0.0.6" :git/sha "7548c3c"}}
                  :ns-default greenlabs.build}}
```

## 태스크 설명

### 1. uberjar 빌드
```sh
clojure -T:build uber \
    :uber-file target/farmmorning-backend.jar \
    :main farmmorning.core
```

### 2. uberjar 빌드 (serverless 프로젝트 전용)

```sh
clj -T:build uber-serverless
```

`target` 하위에 현재 저장소의 sha를 기준으로한 아티팩트가 생성됩니다. 


### 3. serverless 기본 설정 생성

```sh
clj -T:build serverless

cat serverless.yml
```

템플릿에 채워넣을 값은 `deps.edn`에서 지정할 수 있습니다.

```clojure
:aliases {:build {:deps       {io.github.green-labs/build-clj
                               {:git/tag "..." :git/sha "..."}}
                  :ns-default greenlabs.build
                  :exec-args  {:serverless {:service  "my-service"
                                            :function "my-function"
                                            :file     "my-file"
                                            :role     "arn:aws:iam::887960154422:role/lambda-exec-role"}}}}
```

## ❗️ v0.0.5 이하 -> v0.0.6 마이그레이션 가이드
v0.0.5까지 `uber` 로 쓰이던 태스크의 이름이 v0.0.6에서 `uber-serverless` 로 변경되었습니다.