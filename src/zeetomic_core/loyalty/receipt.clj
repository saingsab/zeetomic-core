(ns zeetomic_core.loyalty.receipt
  (:require [zeetomic-core.db.receipt :as receipt]
            [zeetomic-core.db.branches :as branches]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.operation.pay :as pay]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.ed :as ed]
            [ring.util.http-response :refer :all]))

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
  [token receipt-no amount location approval-code]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (try
        (if (= true (approve? location approval-code))
          (try
            (reset! txid (java.util.UUID/randomUUID))
            (future (Thread/sleep 3000)
                    (try
                      (pay/reward! location (get (users/get-users-by-id conn/db {:ID created-by}) :wallet) (str (reward location (Float. amount))) @txid)
                      (receipt/add-receipt conn/db {:ID @txid
                                                    :RECEIPT_NO receipt-no
                                                    :AMOUNT (Float. amount)
                                                    :LOCATION location
                                                    :REWARDS (reward location (Float. amount))
                                                    :STATUS "Pendding"
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
      (ok (receipt/get-receipt-by-owner conn/db {:CREATED_BY (get (auth/token? token) :_id)}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))