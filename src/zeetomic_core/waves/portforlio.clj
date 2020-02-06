(ns zeetomic-core.waves.portforlio
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

(defn portforlio 
    [wallet]
    (json/read-str (get 
        (client/get (str (get env :wavsenodesrv) "/assets/balance/" wallet )) :body) :key-fn keyword))
    
(defn get-portforlio 
    [token]
    (if (= (auth/authorized? token) true)
        (try
            (let [wallet (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)]
             (ok (portforlio  wallet))) 
            (catch Exception ex
                (writelog/tx-log! (str "FAILDED : Wave Portforlio" (.getMessage ex)))
                (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))