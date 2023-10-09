(ns clinic.fhir.client
  (:require [cheshire.core :as json]
            [clj-http.client :as c]))

(defn create!
  "Performs a HTTP POST request on a FHIR server at the given `base-url` for a
   given `resource` with given HTTP `headers`.

   Returns HTTP response of the server after JSON parsing its body."
  [base-url resource headers]
  (-> (if (= "Bundle" (resource :resourceType))
        base-url ; Bundle resources should POST at the server root
        (str base-url "/" (resource :resourceType)))
      (c/post {:headers (into {"Content-Type" "application/fhir+json"} headers)
               :body (json/generate-string resource)
               :throw-exceptions false})
      (update :body json/parse-string true)))
