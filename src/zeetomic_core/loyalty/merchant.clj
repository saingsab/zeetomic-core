(ns zeetomic-core.loyalty.merchant
  (:require [zeetomic-core.db.merchant :as merchant]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all]))

(defn add-merchant!
  [token merchant-name short-name]
  (if (= (auth/authorized? token) true)
    (try
      (merchant/add-merchants conn/db {:ID (java.util.UUID/randomUUID) :MERCHANT_NAME merchant-name :SHORTNAME short-name :CREATED_BY (get (auth/token? token) :_id)})
      (ok {:message "Successfully added merchant"})
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn update-merchant?
  [token id merchant-name short-name]
  (if (= (auth/authorized? token) true)
    (try
      (merchant/update-merchants conn/db {:ID id :MERCHANT_NAME merchant-name :SHORTNAME short-name})
      (ok {:message "Successfully updated merchant"})
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))


(defn get-merchant-by-name
  [token merchant-name]
  (if (= (auth/authorized? token) true)
    (try
      (merchant/get-merchants-by-name conn/db {:MERCHANT_NAME merchant-name})
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-merchant-by-owner
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (merchant/get-merchants-by-owner conn/db {:CREATED_BY (get (auth/token? token) :_id)})
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-all-merchants
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (merchant/get-all-merchants conn/db)
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))