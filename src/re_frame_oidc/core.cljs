(ns re-frame-oidc.core
  (:require [cljsjs.oidc-client :refer [UserManager]]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frame-oidc.internals :as internals]))

(def default-user-manager-name ::default)

(defn create-user-manager
  [config]
  (UserManager. (clj->js config)))

(defn- reg-user-manager
  [user-manager name event-handlers]
  (let [{:keys [user-loaded
                user-unloaded
                access-token-expiring
                access-token-expired
                user-signed-out
                silent-renew-error]} event-handlers]
    (doto user-manager.events
      (.addUserLoaded
       #(do (rf/dispatch [::internals/on-user-loaded name %])
            (when user-loaded (user-loaded %))))
      (.addUserUnloaded
       #(do (rf/dispatch [::internals/on-user-unloaded name])
            (when user-unloaded (user-unloaded))))
      (.addAccessTokenExpired
       #(do (rf/dispatch [::internals/on-access-token-expired name])
            (when access-token-expired (access-token-expired))))
      (.addUserSignedOut
       #(do (rf/dispatch [::internals/on-user-signed-out name])
            (when user-signed-out (user-signed-out)))))
    (when access-token-expiring
      (.addAccessTokenExpiring user-manager.events #(access-token-expiring)))
    (when silent-renew-error
      (.addSilentRenewError user-manager.events #(silent-renew-error %)))))

(rf/reg-event-fx
 ::reg-user-manager
 (fn [_ [_ user-manager name event-handlers]]
   (let [inst-name (or name default-user-manager-name)]
     (reg-user-manager user-manager inst-name (or event-handlers {}))
     {:dispatch [::internals/init inst-name]})))

;; ============================================================================
;; COMPONENTS
;; ============================================================================

(defn- callback-mounted
  [{:keys [user-manager instance-name on-success-fn on-error-fn]}]
  (let [inst-name (or instance-name default-user-manager-name)]
    (rf/dispatch [::internals/on-user-loading inst-name])
    (-> (.signinRedirectCallback user-manager)
        (.then #(do
                  (rf/dispatch [::internals/on-user-loaded inst-name %])
                  (when on-success-fn (on-success-fn %))))
        (.catch #(do
                   (rf/dispatch [::internals/on-user-loading-error inst-name])
                   (when on-error-fn (on-error-fn %)))))))

(defn callback-comp
  [props & children]
  (reagent/create-class
   {:display-name        :callback-comp
    :component-did-mount #(callback-mounted props)
    :reagent-render
    (fn []
      (into [:div] (reagent/children (reagent/current-component))))}))

;; ============================================================================
;; SUBSCRIPTIONS
;; ============================================================================

(rf/reg-sub
 ::instance
 (fn [db [_ inst-name]]
   (get-in db [:rf-oidc (or inst-name default-user-manager-name)])))

(rf/reg-sub
 ::user
 (fn [_ [_ inst-name]]
   (rf/subscribe [::instance inst-name]))
 (fn [inst _]
   (:user inst)))