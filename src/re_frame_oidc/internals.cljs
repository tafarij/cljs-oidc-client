(ns re-frame-oidc.internals
  (:require [re-frame.core :as rf]))

(defn- update-db
  [db inst-name updates]
  (update-in db [:rf-oidc inst-name] #(merge % updates)))

(defn init
  [db [_ inst-name]]
  (assoc-in db [:rf-oidc inst-name] {:user          nil
                                     :loading-user? false}))

(defn on-user-loading
  [db [_ inst-name]]
  (update-db db inst-name {:loading-user? true}))

(defn on-user-loading-error
  [db [_ inst-name]]
  (update-db db inst-name {:loading-user? false}))

(defn on-user-loaded
  [db [_ inst-name user]]
  (update-db db inst-name {:user user
                           :loading-user? false}))

(defn on-user-unloaded
  [db [_ inst-name]]
  (update-db db inst-name {:user nil}))

;; ============================================================================
;; EVENT REGISTRATIONS
;; ============================================================================

(rf/reg-event-db ::init init)
(rf/reg-event-db ::on-user-loading on-user-loading)
(rf/reg-event-db ::on-user-loading-error on-user-loading-error)
(rf/reg-event-db ::on-user-loaded on-user-loaded)
(rf/reg-event-db ::on-user-unloaded on-user-unloaded)
(rf/reg-event-db ::on-access-token-expired on-user-unloaded)
(rf/reg-event-db ::on-user-signed-out on-user-unloaded)