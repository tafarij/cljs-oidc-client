(ns re-frame-oidc.core
  (:require [cljsjs.oidc-client :refer [UserManager]]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frame-oidc.internals :as internals]))

(def default-user-manager-name ::default)

(defn create-user-manager
  [config]
  (UserManager. (clj->js config)))

(defn reg-user-manager
  ([user-manager]
   (reg-user-manager default-user-manager-name user-manager))
  ([user-manager name]
   (doto user-manager.events
     (.addUserLoaded #(rf/dispatch [::internals/on-user-loaded name %]))
     (.addUserUnloaded #(rf/dispatch [::internals/on-user-unloaded name]))
     (.addAccessTokenExpired #(rf/dispatch [::internals/on-access-token-expired name]))
     (.addUserSignedOut #(rf/dispatch [::internals/on-user-signed-out name])))))

(rf/reg-event-fx
 ::reg-user-manager
 (fn [_ [_ user-manager name]]
   (let [inst-name (or name default-user-manager-name)]
     (reg-user-manager user-manager inst-name)
     {:dispatch [::internals/init inst-name]})))

(defn callback-comp
  [options & children]
  (let [{:keys [user-manager instance-name on-success-fn on-error-fn]} options
        inst-name (or instance-name default-user-manager-name)]
    (reagent/create-class
     {:display-name ::callback-comp

      :component-did-mount
      (fn []
        (-> (.signinRedirectCallback user-manager)
            (.then #(do
                      (rf/dispatch [::internals/on-user-loaded inst-name %])
                      (when on-success-fn (on-success-fn %))))
            (.catch #(do
                       (rf/dispatch [::internals/on-user-loading-error])
                       (when on-error-fn (on-error-fn %))))))

      :reagent-render
      (fn []
        (into [:div] (reagent/children (reagent/current-component))))})))

