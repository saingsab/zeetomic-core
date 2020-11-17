(ns zeetomic_core.sdm.sdm-order-status
  (:require [zeetomic-core.db.sdm.sdm-order-status :as sdm-order-status]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))

; List current order status
(defn get-sdm-order-status
    [token]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-order-status/get-sdm-order-status conn/db))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-sdm-order-status " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}}))