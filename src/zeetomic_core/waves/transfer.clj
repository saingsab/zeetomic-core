(ns zeetomic-core.waves.transfer
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
            [buddy.hashers :as hashers]
            [aero.core :refer (read-config)]))

(def env (read-config ".config.edn"))

; PRD need to fetch the AssetID from AssetCode
; (defn get-assetId 
;   [assetCode]
;   (client/get ))
  
(defn send-payment 
    ; [asset-code recipient assetId feeAssetId])
    [token amount recipient]
    (if (= (auth/authorized? token) true)
      (try
        (let [seed (get (users/get-seed-by-id conn/db {:ID (get (auth/token? token) :_id)}) :seed)]
          (println (ed/decrypt seed))
          (ok (json/read-str (get 
            (client/post (str (get env :wavsenode)"/transfer")
                        {:form-params {:seed (ed/decrypt seed) 
                                      :amount amount
                                      :recipient recipient}
                        :content-type :json})
          :body) :key-fn keyword)))
      (catch Exception ex
        (writelog/tx-log! (str "FAILDED : SEND-PAYMENT " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
      (unauthorized {:error {:message "Unauthorized operation not permitted"}})))