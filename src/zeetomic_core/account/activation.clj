(ns zeetomic-core.account.activation
  (:require [clojure.tools.logging :as log]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as status]))
(def status
  (get (status/get-status-by-name conn/db {:STATUS_NAME "active"}) :ID))

(defn temp-tokens
  [id]
  (users/get-users-token conn/db {:ID id}))

(defn user-by-phone
  [phone]
  (users/get-users-by-phone conn/db {:PHONENUMBER phone}))

(defn activate-user
  [user-id temp-token]
  (if (= (get (temp-tokens user-id) :temp_token) temp-token)
    (try
      (users/user-activation conn/db {:ID user-id :TEMP_TOKEN "0" :STATUS_ID status})
      true
      (catch Exception ex
        (log/error ex)))
    false))