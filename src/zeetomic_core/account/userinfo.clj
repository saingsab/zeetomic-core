(ns zeetomic-core.account.userinfo
  (:require [clojure.tools.logging :as log]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as status]
            [zeetomic-core.middleware.auth :as auth]))

(def status
  (get (status/get-status-by-name conn/db {:STATUS_NAME "active"}) :id))

(defn phone-not-exist?
  [phone]
  (nil? (users/get-users-by-phone conn/db {:PHONENUMBER phone})))

(defn email-not-exist?
  [email]
  (nil? (users/get-users-by-mail conn/db {:EMAIL email})))

(defn setup-profile!
  [token first-name mid-name last-name gender]
  (if (= (auth/authorized? token) true)
  ; letdo
    (try
      (users/setup-user-profile conn/db {:ID (get (auth/token? token) :_id) :FIRST_NAME first-name :MID_NAME mid-name :LAST_NAME last-name :GENDER gender :STATUS_ID status})
      {:message "Your profile have been saved successfully"}
      (catch Exception ex
        (.getMessage ex)))
    {:error {:message "Internal server error"}}))

(defn get-user-profile
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)})
      (catch Exception ex
        (.getMessage ex)))
    {:error {:message "Internal server error"}}))