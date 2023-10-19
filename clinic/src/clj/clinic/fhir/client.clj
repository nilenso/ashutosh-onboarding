(ns clinic.fhir.client
  (:require [cheshire.core :as json]
            [clj-http.client :as http]))

(defn create!
  "Performs a HTTP POST request on a FHIR server at the given `base-url` for a
   given `resource` with given HTTP `headers`.

   Returns the HTTP response of the server after JSON parsing its body."
  [base-url resource headers]
  (-> (if (= "Bundle" (resource :resourceType))
        base-url ; Bundle resources should POST at the server root
        (str base-url "/" (resource :resourceType)))
      (http/post {:headers (into {"Content-Type" "application/fhir+json"} headers)
                  :body (json/generate-string resource)
                  :throw-exceptions false})
      (update :body json/parse-string true)))

(defn get-all
  "Searches the given `resource-type` on a FHIR server at the given `base-url`
   and appends the given `query-params` to the request for filtering the search
   results.

   Returns the HTTP response of the server (FHIR Bundle with `searchset` type)
   after JSON parsing its body."
  [base-url resource-type query-params]
  (-> base-url
      (str "/" resource-type)
      (http/get {:headers {"Accept" "application/fhir+json"}
                 :query-params query-params
                 :throw-exceptions false})
      (update :body json/parse-string true)))
