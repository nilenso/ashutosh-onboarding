(ns reagent-sandbox.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

(defn deep-merge [& maps]
  (reduce
   (fn [result current]
     (merge-with
      (fn [x y]
        (cond (map? y) (deep-merge x y)
              (vector? y) (into x y)
              :else y))
      result
      current))
   (first maps)
   (rest maps)))

(defn extend-props [component props]
  (deep-merge props (r/props component)))

(defn flex []
  (let [this (r/current-component)]
    (into
     [:div (extend-props
            this
            {:style {:display "flex"
                     :align-items "center"}})]
     (r/children this))))

(defn flex-row []
  (let [this (r/current-component)]
    (into
     [flex (extend-props this {:style {:flex-flow "row"}})]
     (r/children this))))

(defn flex-column []
  (let [this (r/current-component)]
    (into
     [flex (extend-props
            this
            {:style {:flex-flow "column"}})]
     (r/children this))))


(defn counter-buttons [counter-atom]
  (let [a (r/cursor counter-atom [:additions])
        d (r/cursor counter-atom [:deletions])]
    (fn []
      [flex-row {:style {:gap "10px"}}
       [:button {:on-click #(r/rswap! a inc)} "Add"]
       [:button {:on-click #(r/rswap! d inc)} "Delete"]])))

;; (defn counter-status [counter-atom]
;;   (let [status (r/reaction (let [a (get @counter-atom :additions)
;;                                  d (get @counter-atom :deletions)]
;;                              (cond
;;                                (= a d) "Zero"
;;                                (> a d) "Positive"
;;                                :else "Negative")))]
;;     (fn [] [:p "Counter Status: " @status])))

(defn counter-status [counter-atom]
  (let [a (r/cursor counter-atom [:additions])
        d (r/cursor counter-atom [:deletions])
        status (r/track
                (fn get-counter-status []
                  (cond
                    (= @a @d) "Zero"
                    (> @a @d) "Positive"
                    :else "Negative")))]
    (fn [] [flex-column {:style {:gap "16px"}}
            [:p {:style {:margin 0}} "Addtitions: " @a " Deletions: " @d]
            [:p {:style {:margin 0}} "Counter Status: " @status]])))

(defn home []
  (let [counter-atom (r/atom {:additions 0
                              :deletions 0})]
    (fn []
      [flex-column {:style {:gap "32px"}}
       [counter-buttons counter-atom]
       [counter-status counter-atom]])))

(defn mount-root []
  (d/render [home] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
