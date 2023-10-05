(defproject clinic "0.1.0-SNAPSHOT"
  :description "Onboarding project #2"
  :url "https://github.com/nilenso/ashutosh-onboarding/blob/main/clinic"
  :dependencies [[aero "1.1.6"]
                 [mount "0.1.17"]
                 [org.clojure/clojure "1.11.1"]]
  :source-paths ["src/clj"]
  :main ^:skip-aot clinic.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :plugins [[lein-cloverage "1.2.2"]])
