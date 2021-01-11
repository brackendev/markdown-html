(ns markdowneditor.main
  (:require [reagent.core :as reagent]
            ["showdown" :as showdown]))

(defonce flash-message (reagent/atom nil))
(defonce flash-timeout (reagent/atom nil))

(defn flash
      ([text] (flash text 3000))
      ([text ms]
       (js/clearTimeout @flash-timeout)
       (reset! flash-message text)
       (reset! flash-timeout (js/setTimeout #(reset! flash-message nil) ms))))

(defonce text-state (reagent/atom {:format :md
                                   :value  ""}))

;; https://www.npmjs.com/package/showdown
(defonce showdown-converter (showdown/Converter.))

(defn md->html [md]
      (.makeHtml showdown-converter md))

(defn html->md [html]
      (.makeMarkdown showdown-converter html))

;; https://hackernoon.com/copying-text-to-clipboard-with-javascript-df4d4988697f
(defn copy-to-clipboard [s]
      (let [el (.createElement js/document "textarea")
            selected (when (pos? (-> js/document .getSelection .-rangeCount))
                           (-> js/document .getSelection (.getRangeAt 0)))]
           (set! (.-value el) s)
           (.setAttribute el "readonly" "")
           (set! (-> el .-style .-position) "absolute")
           (set! (-> el .-style .-left) "-9999px")
           (-> js/document .-body (.appendChild el))
           (.select el)
           (.execCommand js/document "copy")
           (-> js/document .-body (.removeChild el))
           (when selected
                 (-> js/document .getSelection .removeAllRanges)
                 (-> js/document .getSelection (.addRange selected)))))

(defn ->md [{:keys [format value]}]
      (case format
            :md value
            :html (html->md value)))

(defn ->html [{:keys [format value]}]
      (case format
            :md (md->html value)
            :html value))

(defn app []
      [:div#row-content.row.justify-content-center.vh-100

       [:div.toast.show {:role        "alert"
                         :aria-live   "assertive"
                         :aria-atomic "true"
                         :style       {:position   :absolute
                                       :margin     :auto
                                       :left       0
                                       :right      0
                                       :text-align :center
                                       :width      200
                                       :z-index    100
                                       :transform  (if @flash-message
                                                     "scaleY(1)"
                                                     "scaleY(0)")
                                       :transition "transform 0.2s ease-out"}}
        [:div.toast-body @flash-message]]

       [:div.col-6.bg-primary
        [:div.d-flex.flex-column.h-100

         [:div.row.justify-content-center.flex-grow-1
          [:div.card.w-100.my-1.mx-1
           [:div.card-header.d-flex.justify-content-between
            [:div "Markdown"]
            [:div [:button.btn-link.btn-primary
                   {:on-click (fn [e]
                                  (copy-to-clipboard (->md @text-state))
                                  (flash "Markdown copied to clipboard"))}
                   [:i.fa.fa-1x.fa-fw.fa-copy] "Copy"]]]
           [:div.card-body.h-100
            [:textarea.form-control.w-100.h-100
             {:on-change (fn [e]
                             (reset! text-state {:format :md
                                                 :value  (-> e .-target .-value)}))
              :value     (->md @text-state)}]]]]

         [:div.row.justify-content-center.flex-grow-1
          [:div.card.w-100.my-1.mx-1
           [:div.card-header.d-flex.justify-content-between
            [:div "HTML"]
            [:div [:button.btn-link.btn-primary
                   {:on-click (fn [e]
                                  (copy-to-clipboard (->html @text-state))
                                  (flash "HTML copied to clipboard"))}
                   [:i.fa.fa-1x.fa-fw.fa-copy] "Copy"]]]
           [:div.card-body.h-100
            [:textarea.form-control.w-100.h-100
             {:on-change (fn [e]
                             (reset! text-state {:format :html
                                                 :value  (-> e .-target .-value)}))
              :value     (->html @text-state)}]]]]]]

       [:div.col-6.bg-primary
        [:div.d-flex.flex-column.h-100
         [:div.row.justify-content-center.bg-primary.flex-grow-1
          [:div.card.w-100.my-1.mx-1
           [:div.card-header "Preview"]
           [:div.card-body
            {:dangerouslySetInnerHTML {:__html (->html @text-state)}}]]]]]])

(defn mount! []
      (reagent/render [app]
                      (.getElementById js/document "app")))

(defn main! []
      (println "main")
      (mount!))

(defn reload! []
      (println "reload")
      (mount!))
