(ns zeetomic_core.sdm.sdm-product-categories
  (:require [zeetomic-core.db.sdm.sdm-product-categories :as sdm-product-categories]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))
        
; List current weight options
(defn get-sdm-product-categories
    [token]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-product-categories/get-sdm-product-categories conn/db))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-sdm-product-categories " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}}))