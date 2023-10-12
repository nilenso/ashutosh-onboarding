(ns clinic.router
  (:require [bidi.bidi :as bidi]
            [clinic.views.core :as views]
            [pushy.core :as pushy]))

(def routes ["/" {"" ::views/home
                  "patients/" {"new" ::views/create-patient}}])

(def history
  (let [dispatch #(views/set-current (:handler %))
        match (partial bidi/match-route routes)]
    (pushy/pushy dispatch match)))

(defn start! []
  (pushy/start! history))
