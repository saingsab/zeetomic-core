(ns zeetomic-core.loyalty.merchant
  (:require [zeetomic-core.db.merchant :as merchant]
            [zeetomic-core.db.apiacc :as apiacc]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all])
  (:import java.util.Base64))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn encode [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn is-partner?
  [email]
  (= true (get (users/get-users-by-mail conn/db {:EMAIL email}) :is_partner)))         
    
(def user-id (atom (uuid)))
  

(defn add-merchant!
  [token merchant-name short-name]
  (if (= (auth/authorized? token) true)
    (try
      (let [created-by (get (auth/token? token) :_id)]
        (merchant/add-merchants conn/db {:ID (java.util.UUID/randomUUID) :MERCHANT_NAME merchant-name :SHORTNAME short-name :CREATED_BY created-by})
        (users/join-partners conn/db {:ID created-by}))
      (ok {:message "Successfully added merchant"})
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn update-merchant?
  [token id merchant-name short-name]
  (if (= (auth/authorized? token) true)
    (try
      (merchant/update-merchants conn/db {:ID id :MERCHANT_NAME merchant-name :SHORTNAME short-name})
      (ok {:message "Successfully updated merchant"})
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-merchant-by-name
  [token merchant-name]
  (if (= (auth/authorized? token) true)
    (try
      (ok (merchant/get-merchants-by-name conn/db {:MERCHANT_NAME merchant-name}))
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-merchant-by-owner
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (merchant/get-merchants-by-owner conn/db {:CREATED_BY (get (auth/token? token) :_id)}))
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-all-merchants
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (merchant/get-all-merchants conn/db))
      (catch Exception ex
        (writelog/op-log! (.getMessage ex))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))


  (defn if-apikey-exist? 
    [apikey]
    (apiacc/get-api-by-id conn/db {:APIKEY apikey}))

  ; If no ke it's generate new key 
  (defn get-apikey
    [token]
    (if (= (auth/authorized? token) true)
      (if (is-partner? (get (users/get-all-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :email)) 
      (if (nil? (if-apikey-exist? (get (auth/token? token) :_id))) 
        (try 
          (reset! user-id (uuid))
          (apiacc/set-apikey conn/db {:ID @user-id :APIKEY (get (auth/token? token) :_id) :APISEC (encode (str @user-id token))})
          (ok {:message {:apikey (get (auth/token? token) :_id) :apisec (encode (str @user-id token))}})
          (catch Exception ex
            (writelog/op-log! (str "ERROR : get-apikey " (.getMessage ex)))
            (ok {:error {:message "Something went wrong on our end"}})))
        (ok {:message {:apikey (get (if-apikey-exist? (get (auth/token? token) :_id)) :apikey) 
                       :apisec (get (if-apikey-exist? (get (auth/token? token) :_id)) :apisec)}}))
        (ok {:error {:message "Your email address does not associated with partner program"}}))
      (unauthorized {:error {:message "Unauthorized operation not permitted"}})))             