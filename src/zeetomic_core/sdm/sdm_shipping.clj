(ns zeetomic_core.sdm.sdm-shipping
  (:require [zeetomic-core.db.sdm.sdm-shipping-services :as sdm-shipping-services]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))
        
; ADDING NEW SHIPPING DONE AT DB Layer

; List current shipping service
(defn get-shipping-services
    [token]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-shipping-services/get-sdm-shipping-services conn/db))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-users-by-owner " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}}))