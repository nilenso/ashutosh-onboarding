(ns reagent-sandbox.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

(defn counter [counter-atom]
  (let [a (r/cursor counter-atom [:additions])
        d (r/cursor counter-atom [:deletions])]
    (fn []
      [:div {:style {:display "flex"
                     :flex-flow "column"
                     :align-items "center"}}

       [:p "Addtitions: " @a " Deletions: " @d]
       [:div {:style {:display "flex" :gap "10px"}}
        [:button {:on-click #(r/rswap! a inc)} "Add"]
        [:button {:on-click #(r/rswap! d inc)} "Delete"]]])))

(defn counter-status [counter-atom]
  (let [status (r/reaction (let [a (get @counter-atom :additions)
                                 d (get @counter-atom :deletions)]
                             (cond
                               (= a d) "Zero"
                               (> a d) "Positive"
                               :else "Negative")))]
    (fn [] [:p "Counter Status: " @status])))

(defn home []
  (let [counter-atom (r/atom {:additions 0
                              :deletions 0})]
    (fn []
      [:div {:style {:display "flex"
                     :flex-flow "column"
                     :align-items "center"
                     :gap "20px"}}
       [counter counter-atom]
       [counter-status counter-atom]])))

(defn mount-root []
  (d/render [home] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
