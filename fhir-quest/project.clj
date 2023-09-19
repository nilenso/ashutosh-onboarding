(defproject fhir-quest "0.1.0-SNAPSHOT"
  :description "Simple analysis queries on (generated) medical data conforming to FHIR R4 data specifications."
  :url "https://github.com/nilenso/ashutosh-onboarding/blob/main/fhir-quest"
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot fhir-quest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
