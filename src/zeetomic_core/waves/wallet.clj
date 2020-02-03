(ns zeetomic-core.waves.wallet
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            [buddy.hashers :as hashers]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.util.ed :as ed]
            [clojure.data.json :as json]
            [zeetomic-core.operation.addasset :as addasset]
            [aero.core :refer (read-config)]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]))

(def env (read-config ".config.edn"))

(defn wallets []
  (try
    (json/read-str 
        (get (client/post (str (get env :wavsenode)"/wallet")) :body) 
        :key-fn keyword)
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
    ; True
      (try
        ; (future (Thread/sleep 5000)
        (reset! xwallet (wallets))
                (try
                  (users/setup-user-wallet conn/db {:ID (get (auth/token? token) :_id) :WALLET (get @xwallet :wallet) :SEED (ed/encrypt (get @xwallet :seed)) :PIN (hashers/derive pin)})
                ;   (addasset/add-assets! (get @xwallet :seed) "ZTO" (get env :assetIssuer))
                  (catch Exception ex
                    (writelog/op-log! (str "ERROR : Setup Wallet " (.getMessage ex)))))
        (ok {:message (wallets)})
        (catch Exception ex
          (writelog/op-log! (str "ERROR : " (.getMessage ex)))
          {:error {:message "Internal server error"}}))
    ; False
      (ok {:message "Opp! look like you already had a wallet"}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))