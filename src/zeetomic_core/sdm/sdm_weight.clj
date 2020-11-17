(ns zeetomic_core.sdm.sdm-weight
  (:require [zeetomic-core.db.sdm.sdm-weight-options :as sdm-weight-options]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))
        
; List current weight options
(defn get-sdm-weight-options
    [token]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-weight-options/get-sdm-weight-options conn/db))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-sdm-weight-options " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}}))