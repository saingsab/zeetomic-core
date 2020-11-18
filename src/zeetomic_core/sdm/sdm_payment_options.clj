(ns zeetomic_core.sdm.sdm-payment-options
  (:require [zeetomic-core.db.sdm.sdm-payment-options :as sdm-payment-options]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))
        
; List current payment options
(defn get-sdm-payment-options
    [token]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-payment-options/get-sdm-payment-options conn/db))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-sdm-payment-options " (.getMessage ex)))
          {:error {:message "Internal server error"}}))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))