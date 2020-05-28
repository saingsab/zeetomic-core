(ns zeetomic_core.loyalty.receipt
  (:require [zeetomic-core.db.receipt :as receipt]
            [zeetomic-core.db.branches :as branches]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.operation.pay :as pay]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.ed :as ed]
            [ring.util.http-response :refer :all]
            [clojure.instant :as instant]))

(def txid (atom ""))

(defn reward
  [branches-name amount]
  (let [reward-rates (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name}) :reward_rates)]
    (* amount reward-rates)))

(defn tx-reward!
  [branches-name destination amount]
  (pay/reward! branches-name destination (str amount)))

(defn approve?
  [branches-name approval-code]
  (if (= approval-code (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name}) :approval_code))
    true
    false))

(defn add-receipt!
  [token receipt-no amount location image-uri approval-code]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (try
        (if (= true (approve? location approval-code))
          (try
            (reset! txid (java.util.UUID/randomUUID))
            (future (Thread/sleep 3000)
                    (try
                      (future (Thread/sleep 3000)
                              (pay/reward! location (get (users/get-users-by-id conn/db {:ID created-by}) :wallet) (str (reward location (Float. amount))) @txid))
                      (receipt/add-receipt conn/db {:ID @txid
                                                    :RECEIPT_NO receipt-no
                                                    :AMOUNT (Float. amount)
                                                    :LOCATION location
                                                    :REWARDS (reward location (Float. amount))
                                                    :STATUS "Pendding"
                                                    :IMAGE_URI image-uri
                                                    :CREATED_BY created-by})
                      (ok {:message "Your transaction has been submitted!"})
                      (catch Exception ex
                        (writelog/op-log! (str "ERROR : " (.getMessage ex))))))
            (catch Exception ex
              (writelog/op-log! (str "ERROR : " (.getMessage ex)))))
          (ok {:error {:message "Declined - Approval code is not valid!"}}))

        (catch Exception ex
          (writelog/op-log! (str "ERROR : " (.getMessage ex)))
          (ok {:error {:message "Something went wrong on our end"}}))))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))




(defn get-receipt
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (receipt/get-receipt-by-owner conn/db {:UPDATED_BY (get (auth/token? token) :_id)}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-reports
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (receipt/transactions-report conn/db {:CREATED_BY (get (auth/token? token) :_id)}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : get-reports " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-reports-from-to-date
  [token from-date to-date]
  (if (= (auth/authorized? token) true)
    (try
      (ok (receipt/trx-from-to-date conn/db {:CREATED_BY (get (auth/token? token) :_id)
                                             :FROM_DATE from-date
                                             :TO_DATE to-date}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : FN get-reports-from-to-date " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-trx-from-to-date-by-location
  [token from-date to-date location]
  (if (= (auth/authorized? token) true)
    (try
      (ok (receipt/trx-from-to-date-by-location conn/db {:CREATED_BY (get (auth/token? token) :_id)
                                                         :FROM_DATE from-date
                                                         :TO_DATE to-date
                                                         :LOCATION location}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : FN get-reports-from-to-date " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-trx-by-location
  [token location]
  (if (= (auth/authorized? token) true)
    (try
      (ok (receipt/trx-by-location conn/db {:CREATED_BY (get (auth/token? token) :_id)
                                            :LOCATION location}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : FN get-reports-from-to-date " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))