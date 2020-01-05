(ns zeetomic-core.operation.addasset
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.ed :as ed]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            [clojure.data.json :as json]
            [aero.core :refer (read-config)]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]))

(def env (read-config ".config.edn"))

(defn add-assets!
  [akey assetCode assetIssuer]
  (println "Change Trust...")
  (try
    (catch Exception ex
      (writelog/op-log! (str "ERROR : Change Trust " (.getMessage ex)))))
  (client/post (str (get env :addassetendpoint))
               {:form-params {:akey akey
                              :assetCode assetCode
                              :assetIssuer assetIssuer}
                :content-type :json}))

(defn accept-asset?
  [token assetCode assetIssuer]
  (if (= (auth/authorized? token) true)
    (try
      (client/post (str (get env :addassetendpoint))
                   {:form-params {:akey (ed/decrypt (get (users/get-seed-by-id conn/db {:ID (get (auth/token? token) :_id)}) :seed))
                                  :assetCode assetCode
                                  :assetIssuer assetIssuer}
                    :content-type :json})
      (ok {:message (str "You successfully added " assetCode " into your portforilo")})
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        {:error {:message "Internal server error"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))