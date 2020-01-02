(ns zeetomic-core.operation.pay
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.ed :as ed]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.branches :as branches]
            [zeetomic-core.db.receipt :as receipt]
            [clojure.data.json :as json]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all]
            [buddy.hashers :as hashers]
            [aero.core :refer (read-config)]))

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

(defn pay!
  [token pin asset-code destination amount memo]
  (if (= (auth/authorized? token) true)
    (try
      (if (hashers/check pin (get (users/get-pin-by-id conn/db {:ID (get (auth/token? token) :_id)}) :pin))
        (try
          (future (Thread/sleep 3000)
                  (if (= true (enought-balance? (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet) "ZTO" 0.0002))
                    (try
                      (fee (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get (auth/token? token) :_id)}) :seed)))
                      (println "Init payment tx ...")
                      (client/post (str (get env :sendpayment))
                                   {:form-params {:senderKey (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get (auth/token? token) :_id)}) :seed))
                                                  :assetCode asset-code
                                                  :destination destination
                                                  :amount amount
                                                  :memo memo}
                                    :content-type :json})
                      (catch Exception ex
                        (.getMessage ex)))
                    (writelog/tx-log! (str "FAILDED : FN Pay from : " (get (auth/token? token) :_id) " Out of ZTO "))))
          (ok {:message "Your transaction has been submitted!"})
          (catch Exception ex
            (writelog/tx-log! (str "FAILDED : FN Pay from : " (get (auth/token? token) :_id) " Out of ZTO "))))
        (ok {:error {:message "PIN does not correct!"}}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        {:error {:message "Unauthorized operation not permitted"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn reward!
  [branches-name destination amount txid]
  (let [sender (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name})]
    (if (= true (enought-balance? (get (users/get-users-by-id conn/db {:ID (get sender :created_by)}) :wallet) "ZTO" 0.0002))
      (try
        (fee (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get sender :created_by)}) :seed)))
        (receipt/update-receipt-status conn/db {:ID (str txid) :STATUS "Completed"})
        (client/post (str (get env :sendpayment))
                     {:form-params {:senderKey (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get sender :created_by)}) :seed))
                                    :assetCode (get sender :asset_code)
                                    :destination destination
                                    :amount amount
                                    :memo "Reward!"}
                      :content-type :json})

        (catch Exception ex
          (writelog/tx-log! (str "FAILDED : REWARD! From " (get sender :branches_name) " To : " (.getMessage ex)))))
      {:error {:message "Something went wrong on our end"}})))


(defn portforlio [token]
  (if (= (auth/authorized? token) true)
    (try
      (let [wallet (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)]
        (ok (get (json/read-str (get (client/get (str (get env :horizon) "/accounts/" wallet)) :body) :key-fn keyword) :balances)))
      (catch Exception ex
        (writelog/tx-log! (str "FAILDED : fetch portforlio " (.getMessage ex)))))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn trx-hostory [token]
  (if (= (auth/authorized? token) true)
    (try
      (let [wallet (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)]
        (println wallet)
        (ok (get (get (json/read-str (get (client/get (str (get env :horizon) "/accounts/" wallet "/operations?order=desc")) :body) :key-fn keyword) :_embedded) :records)))
      (catch Exception ex
        (writelog/tx-log! (str "FAILDED : fetch tx history " (.getMessage ex)))))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))