(defproject fhir-quest "0.1.0-SNAPSHOT"
  :description "Simple analysis queries on (generated) medical data conforming to FHIR R4 data specifications."
  :url "https://github.com/nilenso/ashutosh-onboarding/blob/main/fhir-quest"
  :dependencies [[cheshire "5.11.0"]
                 [cli-matic "0.5.4"]
                 [clojure.java-time "1.3.0"]
                 [compojure "1.7.0"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.flywaydb/flyway-core "9.22.1"]
                 [org.xerial/sqlite-jdbc "3.43.0.0"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]]
  :source-paths ["src/cli"]
  :main ^:skip-aot fhir-quest.core
  :target-path "target/%s"
  :profiles {:test {:dependencies [[clj-http "3.12.3"]
                                   [ring/ring-mock "0.4.0"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :web-ui {:prep-tasks [["do" "shell" "npx" "shadow-cljs" "compile" "app"]]}}
  :clean-targets [:target-path "synthea"]
  :plugins [[lein-cloverage "1.2.2"]
            [lein-shell "0.5.0"]]
  :aliases {"gen-fhir-data" ["do" "shell" "./scripts/gen-fhir-data.sh" "synthea"]})
