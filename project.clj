 (defproject zeetomic-core "0.1.0"
   :description "ZEETOMIC: Core Operation"
   :dependencies [[org.clojure/clojure "1.10.0"]
                  [metosin/compojure-api "2.0.0-alpha30"]
                  [metosin/ring-swagger-ui "3.20.1"]
                  ; DATABASE
                  [com.layerware/hugsql "0.5.1"]
                  [org.postgresql/postgresql "42.2.2"]
                  ; logging
                  [org.clojure/tools.logging "0.5.0"]
                  ; ENV
                  [aero "1.1.3"]
                  ; mail
                  [com.draines/postal "2.0.3"]
                  ; HTTP Client
                  [clj-http "3.10.0"]
                  ; JSON
                  [org.clojure/data.json "0.2.7"]
                  ; Hash
                  [buddy/buddy-hashers "1.4.0"]
                  [buddy/buddy-auth "2.2.0"]]
   :ring {:handler zeetomic-core.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                    :plugins [[lein-ring "0.12.5"]]}})
