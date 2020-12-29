(ns zeetomic-core.operation.getwallet
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            [buddy.hashers :as hashers]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.apiacc :as apiacc]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.util.ed :as ed]
            [clojure.data.json :as json]
            [zeetomic-core.operation.addasset :as addasset]
            [aero.core :refer (read-config)]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]))

(def env (read-config ".config.edn"))
(defn uuid [] (str (java.util.UUID/randomUUID)))

(def user-id (atom (uuid)))

(defn wallets []
  (try
    (json/read-str
     (get (client/post (str (get env :getwalletendpoint))
                       {:form-params {
                         :label "sr25519"
                         :name "dddd"}
                        :content-type :json}) :body) :key-fn keyword)
    (catch Exception ex
      (writelog/op-log! (str "ERROR : " (.getMessage ex)))
      "Internal server error")))

(def xwallet (atom {:wallet "" :seed ""}))

(defn is-wallet-nil? [id]
  (nil? (get (users/get-users-by-id conn/db {:ID id}) :wallet)))

(defn gen-wallet
  [token pin]
  (if (= (auth/authorized? token) true)
    (if (= true (is-wallet-nil? (get (auth/token? token) :_id)))
    ; Account need to have phone verified.
    ; True
    (if (and (not= (get (users/get-users-token conn/db {:ID (get (auth/token? token) :_id)}) :temp_token) "0") 
             (nil? (get  (users/get-users-token conn/db {:ID (get (auth/token? token) :_id)}) :phonenumber)))
          (ok {:code "001"
               :message "Opp! You need to verify your phone number first"})
          (try
            (reset! xwallet (wallets))
            ; (println  (wallets))
            (future (Thread/sleep 5000)
                    (try
                      (users/setup-user-wallet conn/db {:ID (get (auth/token? token) :_id) :WALLET (get @xwallet :address) :SEED (ed/encrypt (get @xwallet :mnemonic)) :PIN (hashers/derive pin)})
                      ; (addasset/add-assets! (get @xwallet :seed) "SEL" (get env :assetIssuer))
                      (catch Exception ex
                        (writelog/op-log! (str "ERROR : Setup Wallet " (.getMessage ex))))))
            (ok {:message {:wallet (get @xwallet :address) :seed (get @xwallet :mnemonic)}})
            (catch Exception ex
              (writelog/op-log! (str "ERROR : " (.getMessage ex)))
              {:error {:message "Internal server error"}})))
    ; False
      (ok {:message "Opp! look like you already had a wallet"}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

  (defn gen-wallet-by-api
    [apikey apisec]
        (if (and (= apikey (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apikey)) (= apisec (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apisec))) 
          (try 
            ; write account to db
            (reset! xwallet (wallets))
            (reset! user-id (uuid))
            (users/setup-wallet-by-api conn/db {:ID @user-id :WALLET (get @xwallet :address) :SEED (ed/encrypt (get @xwallet :mnemonic)) :CREATED_BY apikey})
            ; (addasset/add-assets! (get @xwallet :seed) "SEL" (get env :assetIssuer))
            ; return wallet 
            (ok {:message {:id @user-id :wallet (get @xwallet :address)}})
            (catch Exception ex
              (writelog/op-log! (str "ERROR : gen-wallet-by-api " (.getMessage ex)))
              {:error {:message "Internal server error"}}))
          (ok {:message {:error "Invalid API KEYS"}})))