(ns zeetomic-core.account.walletlookup
  (:require [clojure.tools.logging :as log]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.middleware.auth :as auth]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]))

;; get-wallet-by-phone
;; Check if phone exist
(defn phone-not-exist?
  [phone]
  (nil? (users/get-users-by-phone conn/db {:PHONENUMBER phone})))

(defn wallet-lookup
  [phone]
  (users/get-wallet-by-phone conn/db {:PHONENUMBER phone}))

(defn get-wallet
  [token phone]
  (if (= (auth/authorized? token) true)
    (if (= true (phone-not-exist? phone))
      (ok {:message "Sorry phone number does not exist!"})
      (try
        (ok (wallet-lookup phone))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-wallet" (.getMessage ex)))
          {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))