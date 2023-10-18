(ns clinic.components
  (:require [reagent.core :as r]))

(defn heading-1 [text]
  [:h1 {:class "text-3xl md:text-4xl"} text])

(defn heading-2 [text]
  [:h2 {:class ["text-xl" "md:text-2xl"]} text])

(defn page []
  (let [this (r/current-component)
        props (r/props this)
        logout-enabled (props :logout-enabled)
        logout-handler (props :on-logout-click #())]
    (into [:main {:class ["flex" "flex-col gap-12 md:gap-16"
                          "w-full max-w-4xl"
                          "mx-auto p-8 md:p-12"]}
           [:header {:class ["flex" "flex-row" "gap-12"]}
            [heading-1 "Acme Orthopedic Clinic"]
            (when logout-enabled
              [:<>
               [:div {:class "flex-grow"}]
               [:button {:on-click logout-handler
                         :class ["bg-transparent" "hover:bg-blue-500" "text-blue-700"
                                 "hover:text-white" "px-4" "py-2"
                                 "border" "border-blue-500"
                                 "hover:border-transparent" "rounded-full"]}
                "Logout"]])]]
          (r/children this))))

(defn text-field []
  (let [{name :name
         label :label
         placeholder :placeholder
         error-msg :error-msg
         touched? :touched?
         invalid? :invalid?} (r/props (r/current-component))]
    [:div {:class ["w-full" "flex" "flex-col" "gap-2"]}
     [:label {:for name
              :class ["block" "uppercase" "tracking-wide" "text-gray-600"
                      "text-xs" "font-bold"]}
      label]

     [:input {:id name
              :name name
              :placeholder placeholder
              :class ["appearance-none" "block" "w-full" "bg-gray-200"
                      "text-gray-700" "border" "border-gray-200"
                      "rounded" "py-3" "px-4" "leading-tight"
                      "focus:outline-none" "focus:bg-white"
                      "focus:border-gray-500"]}]
     [:p {:class [(if (and touched? invalid?) "visible" "invisible")
                  "text-red-500" "text-xs" "italic"]}
      error-msg]]))

(defn select-field [name label default-value options]
  [:div {:class ["w-full" "flex" "flex-col" "gap-2"]}
   [:label {:for name
            :class ["block" "uppercase" "tracking-wide" "text-gray-600"
                    "text-xs" "font-bold"]}
    label]
   [:div {:class ["relative"]}
    (into [:select {:id name
                    :name name
                    :defaultValue default-value
                    :class ["appearance-none" "block" "w-full" "bg-gray-200"
                            "text-gray-700" "border" "border-gray-200"
                            "rounded" "py-3" "px-4" "pr-8" "leading-tight"
                            "focus:outline-none" "focus:bg-white"
                            "focus:border-gray-500"]}]
          (for [[name value] options]
            [:option {:value value} name]))
    [:div
     {:class ["pointer-events-none" "absolute" "inset-y-0" "right-0" "flex"
              "items-center" "px-2" "text-gray-700"]}
     [:svg
      {:class "fill-current h-4 w-4",
       :xmlns "http://www.w3.org/2000/svg",
       :viewBox "0 0 20 20"}
      [:path
       {:d "M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"}]]]]])

(defn button [type text loading]
  [:button
   {:disabled loading
    :type type
    :class [(if loading "bg-blue-400" "bg-blue-600")
            (if loading "hover:bg-blue-400" "hover:bg-blue-800")
            "text-white" "font-medium" "py-2" "px-4" "rounded-full"
            "focus:ring-4" "focus:outline-none" "focus:ring-blue-300"
            "rounded-full" "text-md" "text-center" "dark:bg-blue-600"
            "dark:hover:bg-blue-700" "dark:focus:ring-blue-800"
            "inline-flex" "items-center" "justify-center"]}
   [:svg
    {:class [(if loading "visible" "invisible")
             "inline" "w-6" "h-6" "-ml-9" "mr-3" "text-white" "animate-spin"]
     :aria-hidden "true"
     :role "status"
     :viewBox "0 0 100 101"
     :fill "none"
     :xmlns "http://www.w3.org/2000/svg"}
    [:path
     {:d "M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0
          78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082
          100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013
          91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186
          50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226
          9.08144 27.9921 9.08144 50.5908Z"
      :fill "#E5E7EB"}]
    [:path
     {:d "M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079
          33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452
          15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754
          1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345
          1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873
          9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191
          9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331
          15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175
          28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421
          39.6781 93.9676 39.0409Z",
      :fill "currentColor"}]]
   text])

(defn danger-alert []
  (into [:div
         {:class
          ["flex flex-row items-center gap-4"
           "px-8 py-4 mb-6"
           "rounded-lg bg-red-50"
           "text-sm text-red-800"]
          :role "alert"}
         [:span {:class "font-medium text-3xl"} "⚠️"]]
        (r/children (r/current-component))))
