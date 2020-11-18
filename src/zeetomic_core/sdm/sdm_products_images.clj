(ns zeetomic_core.sdm.sdm-products-images
  (:require [zeetomic-core.db.sdm.sdm-products-images :as sdm-products-images]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))

(def txid (atom ""))

(defn add-sdm-products-images 
  [token url product-id]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (try 
        (reset! txid (java.util.UUID/randomUUID))
        (sdm-products-images/add-sdm-products-images conn/db {:ID @txid
                                                              :URL url
                                                              :PRODUCT_ID product-id 
                                                              :CREATED_BY created-by})
        ; will treat is silent save product image url
    (catch Exception ex
        (writelog/op-log! (str "ERROR : FN add-sdm-products-images " (.getMessage ex)))
        {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

;   list images by products id
(defn get-sdm-products-images-by-product-id 
    [token product-id]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-products-images/get-sdm-products-images-by-product-id conn/db {:PRODUCT_ID product-id}))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-users-by-owner " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}}))
  