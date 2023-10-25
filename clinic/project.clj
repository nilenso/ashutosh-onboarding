(defproject clinic "latest"
  :description "Onboarding project #2"
  :url "https://github.com/nilenso/ashutosh-onboarding/blob/main/clinic"
  :dependencies [[aero "1.1.6"]
                 [clj-http "3.12.3"]
                 [compojure "1.7.0"]
                 [mount "0.1.17"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-json "0.5.1"]]
  :source-paths ["src/cljc" "src/clj"]
  :test-paths ["test/clj"]
  :main ^:skip-aot clinic.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}
             :test {:dependencies [[ring/ring-mock "0.4.0"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :plugins [[lein-cloverage "1.2.2"]])
