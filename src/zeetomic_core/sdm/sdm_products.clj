(ns zeetomic_core.sdm.sdm-products
  (:require [zeetomic-core.db.sdm.sdm-products :as sdm-products]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))
            ; [clojure.instant :as instant]

(def txid (atom ""))

(defn add-products 
  [token name price shipping weight description thumbnail category-id payment-id]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (try 
        (reset! txid (java.util.UUID/randomUUID))
        (sdm-products/add-products conn/db {:ID @txid
                                            :NAME name
                                            :PRICE (Float/parseFloat price)
                                            :SHIPPING shipping
                                            :WEIGHT weight
                                            :DESCRIPTION description
                                            :THUMBNAIL thumbnail
                                            :CATEGORY_ID category-id
                                            :PAYMENT_ID payment-id
                                            :CREATED_BY created-by})
        (ok {:message "The listing has been created"})
    (catch Exception ex
        (writelog/op-log! (str "ERROR : FN add-products " (.getMessage ex)))
        {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-products-by-owner 
    [token]
    (if (= (auth/authorized? token) true)
      (let [created-by (get (auth/token? token) :_id)]
        (try
          (ok (sdm-products/get-products-by-owner conn/db {:CREATED_BY created-by}))
      (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-products-by-owner " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-all-products 
    [token]
    (if (= (auth/authorized? token) true)
        (try
          (ok (sdm-products/get-all-products conn/db))
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-all-products " (.getMessage ex)))
          {:error {:message "Internal server error"}}))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))
  