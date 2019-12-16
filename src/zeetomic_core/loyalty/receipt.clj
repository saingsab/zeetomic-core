(ns zeetomic_core.loyalty.receipt
  (:require [zeetomic-core.db.receipt :as receipt]
            [zeetomic-core.db.branches :as branches]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.operation.pay :as pay]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.ed :as ed]))

(def txid (atom ""))

(defn reward!
  [branches-name amount]
  (let [reward-rates (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name}) :reward_rates)]
    (* amount reward-rates)))

(defn tx-reward!
  [branches-name destination amount]
  (pay/reward! branches-name destination amount))

(defn add-receipt!
  [token receipt-no amount location]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (try
        (reset! txid (java.util.UUID/randomUUID))
        (future (Thread/sleep 3000)
                (pay/reward! location (get (users/get-users-by-id conn/db {:ID created-by}) :wallet) (reward! amount location))
                (receipt/update-receipt-status conn/db {:ID @txid :STATUS "Completed"}))

        (receipt/add-receipt conn/db {:ID @txid
                                      :RECEIPT_NO receipt-no
                                      :AMOUNT (Float. amount)
                                      :LOCATION location
                                      :REWARDS (reward! amount location)
                                      :STATUS "Pendding"
                                      :CREATED_BY created-by})
        {:message "Your transaction has been submitted!"}
        (catch Exception ex
          (writelog/op-log! (str "ERROR : " (.getMessage ex)))
          {:error {:message "Something went wrong on our end"}})))
    {:error {:message "Unauthorized operation not permitted"}}))

(defn get-receipt
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (receipt/get-receipt-by-owner conn/db {:ID (get (auth/token? token) :_id)})
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        {:error {:message "Something went wrong on our end"}}))
    {:error {:message "Unauthorized operation not permitted"}}))