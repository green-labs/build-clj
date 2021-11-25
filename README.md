# build-clj

tools.build 사용하듯이 `:build` 별칭으로 사용할 수 있습니다.
 
```clojure
{,,,
 :aliases {:build {:deps       {io.github.green-labs/build-clj
                                {:git/tag "v0.0.1" :git/sha "015e5e5"}}
                   :ns-default greenlabs.build}}
 ,,,}
```

## 태스크 설명

### 1. 우버좌 빌드

```sh
clj -T:build uber
```

`target` 하위에 현재 저장소의 sha를 기준으로한 아티팩트가 생성됩니다. 


### 2. serverless 기본 설정 생성

```sh
clj -T:build serverless

cat serverless.yml
```

템플릿에 채워넣을 값은 `deps.edn`에서 지정할 수 있습니다.

```clojure
{,,,
 :aliases {:build {:deps       {io.github.green-labs/build-clj
                                {:git/sha "6cc717fba2ec66955268f6296705ede8ed8759fb"}}
                   :ns-default greenlabs.build
                   :exec-args  {:serverless {:service  "my-service"
                                             :function "my-function"
                                             :file     "my-file"
                                             :role     "arn:aws:iam::887960154422:role/lambda-exec-role"}}}}
 ,,,}
```
