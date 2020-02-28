(ns zeetomic-core.sto.whitelist
  (:require [clj-http.client :as client]
            [zeetomic-core.middleware.auth :as auth]
            ;; [zeetomic-core.util.ed :as ed]
            ;; [zeetomic-core.db.users :as users]
            ;; [zeetomic-core.util.conn :as conn]
            [clojure.data.json :as json]
            [aero.core :refer (read-config)]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]))

(def env (read-config ".config.edn"))
(defn whitelist!
  [token trustoracc assetcode authcode]
  (if (= (auth/authorized? token) true)
    (if (= "ABCDEF" authcode)
      (try
        (ok {:message (get
                       (client/post (str (get env :stellar-node) "/whitelist")
                                    {:form-params {:trustoracc trustoracc
                                                   :assetcode assetcode}
                                     :content-type :json})
                       :body)})
        (catch Exception ex
          (writelog/op-log! (str "FAILDED : whitelist " (.getMessage ex)))
          (ok {:error {:message "Internal server error"}})))
      (ok {:error {:message "Authorized code was not corrected"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))
