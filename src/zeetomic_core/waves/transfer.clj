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

(defn send-payment 
    [asset-code recipient assetId feeAssetId])