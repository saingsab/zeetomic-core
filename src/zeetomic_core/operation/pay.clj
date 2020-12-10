(ns zeetomic-core.operation.pay
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.ed :as ed]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.apiacc :as apiacc]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.branches :as branches]
            [zeetomic-core.db.receipt :as receipt]
            [zeetomic-core.db.trxarchive :as trxarchive]
            [clojure.data.json :as json]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all]
            [buddy.hashers :as hashers]
            [aero.core :refer (read-config)]))
          
(def txid (atom ""))

(def env (read-config ".config.edn"))

(def account-info (atom {}))

(defn count-asset
  [wallet]
  (count (get (json/read-str (get (client/get (str (get env :horizon) "/accounts/" wallet)) :body) :key-fn keyword) :balances)))

(defn set-account-info
  [wallet asset-code]
  (loop [i 0]
    (when (< i (count-asset wallet))
      (if (= asset-code (get (nth (get (json/read-str (get (client/get (str (get env :horizon) "/accounts/" wallet)) :body) :key-fn keyword) :balances) i) :asset_code))
        (reset! account-info (nth (get (json/read-str (get (client/get (str (get env :horizon) "/accounts/" wallet)) :body) :key-fn keyword) :balances) i)))
      (recur (+ i 1)))))


(defn enought-balance?
  [wallet asset-code amount]
  (set-account-info wallet asset-code)
  (< amount (Float. (get @account-info :balance))))


(defn fee
  [seed]
  (client/post (str (get env :feecharge))
               {:form-params {:seed seed}
                :content-type :json}))

(defn is-wallet-nil? [id]
  (nil? (get (users/get-users-by-id conn/db {:ID id}) :wallet)))

(defn get-portforlio
  [token]
  (if (= true (is-wallet-nil? (get (auth/token? token) :_id)))
    (ok {:error {:message "Look like you don't have a wallet yet!"}})
    (try
      (ok (json/read-str (get 
                        (client/post (str (get env :selendpoint) "/balances")
                                      {:form-params {:add (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)}
        :content-type :json}) :body) :key-fn keyword))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        "Internal server error"))
  ))

(defn get-trx-hostory
  [wallet]
  (try
    ; (get (get (json/read-str (get (client/get (str (get env :horizon) "/accounts/" wallet "/operations?order=desc")) :body) :key-fn keyword) :_embedded) :records)
    (trxarchive/get-trx-by-account conn/db {:SENDER wallet :DESTINATION wallet})
    (catch Exception ex
      {:error {:message "Look like you don't have a wallet yet!"}})))

(defn pay!
  [token pin asset-code destination amount memo]
  (if (= (auth/authorized? token) true)
    (try
      (if (hashers/check pin (get (users/get-pin-by-id conn/db {:ID (get (auth/token? token) :_id)}) :pin))
        (try
            (reset! txid (java.util.UUID/randomUUID))
            (println "... Init payment tx ...")
            (let [hash (json/read-str 
                          (get (client/post (str (get env :selendpoint) "/transfer")
                                  {:form-params {:sender (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get (auth/token? token) :_id)}) :seed))
                                                  :assetCode asset-code
                                                  :dest destination
                                                  :amount amount
                                                  :memo memo}
                                    :content-type :json}) :body) :key-fn keyword)]
            ; (println (get hash :message))
            ; save trx to local db
            (trxarchive/add-trxarchive conn/db {:ID @txid 
                                                :BLOCK nil 
                                                :HASH (get hash :message) 
                                                :SENDER (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)
                                                :DESTINATION destination 
                                                :AMOUNT (Float/parseFloat amount)
                                                :FEE 0.0001 
                                                :MEMO memo
                                                :CREATED_BY (get (auth/token? token) :_id)}))
          
        (ok {:message "Your transaction is on the way!"})
          (catch Exception ex
            (writelog/tx-log! (str "FAILDED : FN Pay from : " (get (auth/token? token) :_id) " Out of SEL "))))
        (ok {:error {:message "PIN does not correct!"}}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        {:error {:message "Unauthorized operation not permitted"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn reward!
  [branches-name destination amount]
  (let [sender (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name})]
    (try
      (println (str "Start Rewarding....." amount))
      (let [hash (json/read-str 
                          (get :body) :key-fn keyword)])
      (println (client/post (str (get env :selendpoint) "/transfer")
                            {:form-params {:sender (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get sender :created_by)}) :seed))
                                           :assetCode (get sender :asset_code)
                                           :dest destination
                                           :amount amount
                                           :memo "Reward!"}
                             :content-type :json}))
      (println "Rewarding Finished!")
      (catch Exception ex
        (writelog/tx-log! (str "FAILDED : REWARD! From " (get sender :branches_name) " To : " (.getMessage ex)))))))


(defn portforlio [token]
  (if (= (auth/authorized? token) true)
    (try
      (let [wallet (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)]
        (ok (get-portforlio wallet)))
      (catch Exception ex
        (writelog/tx-log! (str "FAILDED : fetch portforlio " (.getMessage ex)))))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn trx-hostory [token]
  (if (= (auth/authorized? token) true)
    (try
      (let [wallet (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)]
        (ok (get-trx-hostory wallet)))
      (catch Exception ex
        (writelog/tx-log! (str "FAILDED : fetch tx history " (.getMessage ex)))))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn pay-by-api
  [id apikey apisec destination asset-code amount memo]
  (if (and (= apikey (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apikey)) (= apisec (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apisec))) 
      (try 
        (reset! txid (java.util.UUID/randomUUID))
        (println "... Init payment tx ...")
        (let [hash (json/read-str 
                          (get (client/post (str (get env :selendpoint) "/transfer")
                                  {:form-params {:sender (ed/decrypt (get (users/get-seed-by-id conn/db {:ID id}) :seed))
                                                  :assetCode asset-code
                                                  :dest destination
                                                  :amount amount
                                                  :memo memo}
                                    :content-type :json}) :body) :key-fn keyword)]
            ; (println (get hash :message))
            ; save trx to local db
            (trxarchive/add-trxarchive conn/db {:ID @txid 
                                                :BLOCK nil 
                                                :HASH (get hash :message) 
                                                :SENDER (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)
                                                :DESTINATION destination 
                                                :AMOUNT (Float/parseFloat amount)
                                                :FEE 0.0001 
                                                :MEMO memo
                                                :CREATED_BY (get (auth/token? token) :_id)}))
        (catch Exception ex
          (writelog/tx-log! (str "FAILDED : FN pay-by-api " (.getMessage ex)))))
    (ok {:message {:error "Invalid API KEYS"}})))

(defn hostory-by-api 
  [apikey apisec wallet]
  (if (and (= apikey (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apikey)) (= apisec (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apisec))) 
    (try 
      (ok (get-trx-hostory wallet))
    (catch Exception ex
      (writelog/tx-log! (str "FAILDED : fetch tx history " (.getMessage ex)))))
  (ok {:message {:error "Invalid API KEYS"}})))

(defn hostory-by-api 
  [apikey apisec wallet]
  (if (and (= apikey (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apikey)) (= apisec (get (apiacc/get-api-by-id conn/db {:APIKEY apikey}) :apisec))) 
    (try 
      (ok (get-trx-hostory wallet))
    (catch Exception ex
      (writelog/tx-log! (str "FAILDED : fetch tx history " (.getMessage ex)))))
  (ok {:message {:error "Invalid API KEYS"}})))