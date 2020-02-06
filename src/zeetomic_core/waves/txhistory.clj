(ns zeetomic-core.waves.txhistory
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.ed :as ed]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            ; [zeetomic-core.db.branches :as branches]
            ; [zeetomic-core.db.receipt :as receipt]
            [clojure.data.json :as json]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all]
            ; [buddy.hashers :as hashers]
            [aero.core :refer (read-config)]))
            
(def env (read-config ".config.edn"))

(defn tx-history 
    [wallet]
        (ok (nth (json/read-str (get 
        (client/get (str (get env :wavsenodesrv) "/transactions/address/" wallet "/limit/10")) :body) :key-fn keyword) 0)))

(defn get-txhistory 
    [token]
    (if (= (auth/authorized? token) true)
        (try 
            (let [wallet (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)]
                (tx-history wallet))
        (catch Exception ex
            (writelog/tx-log! (str "FAILDED : Waves Transaction history" (.getMessage ex)))
                (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))