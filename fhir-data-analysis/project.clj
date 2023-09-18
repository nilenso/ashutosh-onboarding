(defproject fhir-data-analysis "0.1.0-SNAPSHOT"
  :description "Some basic data analysis on FHIR R4 data generated using Synthea"
  :url "https://github.com/nilenso/ashutosh-onboarding"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cheshire "5.11.0"]
                 [clojure.java-time "1.3.0"]]
  :main ^:skip-aot fhir-data-analysis.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
