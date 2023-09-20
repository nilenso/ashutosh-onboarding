(defproject fhir-quest "0.1.0-SNAPSHOT"
  :description "Simple analysis queries on (generated) medical data conforming to FHIR R4 data specifications."
  :url "https://github.com/nilenso/ashutosh-onboarding/blob/main/fhir-quest"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cli-matic "0.5.4"]
                 [org.xerial/sqlite-jdbc "3.43.0.0"]
                 [org.flywaydb/flyway-core "9.22.1"]]
  :main ^:skip-aot fhir-quest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :clean-targets [:target-path "synthea"]
  :plugins [[lein-shell "0.5.0"]]
  :aliases {"gen-fhir-data" ["do" "shell" "./scripts/gen-fhir-data.sh" "synthea"]})
