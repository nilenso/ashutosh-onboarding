(ns clinic.user-role.effects
  (:require [re-frame.core :as rf]
            [reagent.cookies :as cookies]))

(rf/reg-fx ::set-cookie
           (fn set-cookie-effect [user-role]
             (cookies/set! ::user-role user-role {:max-age -1
                                                  :path "/"
                                                  :secure? false})))

(rf/reg-cofx ::get-cookie
             (fn get-cookie-coeffect [cofx default-role]
               (assoc cofx ::get-cookie (cookies/get ::user-role default-role))))
