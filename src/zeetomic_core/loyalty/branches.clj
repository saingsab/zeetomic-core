(ns zeetomic-core.loyalty.branches
  (:require [zeetomic-core.db.branches :as branches]
            [zeetomic-core.db.merchant :as merchant]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.util.writelog :as writelog]))

(defn add-branches!
  [token merchant-id branches-name address reward-rates asset-code minimum-spend approval-code]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (if (= created-by (get (merchant/get-merchants-by-owner conn/db {:CREATED_BY created-by}) :created_by))
        (try
          (branches/add-branches conn/db {:ID (java.util.UUID/randomUUID)
                                          :MERCHANTS_ID merchant-id ;"3af5aa71-dc5d-4534-9a15-a5317065115e"
                                          :BRANCHES_NAME branches-name
                                          :ADDRESS address
                                          :REWARD_RATES (Float. reward-rates)
                                          :ASSET_CODE asset-code
                                          :MINIMUM_SPEND  (Float. minimum-spend)
                                          :APPROVAL_CODE approval-code
                                          :CREATED_BY created-by})
          {:message "Successfully added branches"}
          (catch Exception ex
            (writelog/op-log! (str "ERROR : " (.getMessage ex)))
            {:error {:message "Something went wrong on our end"}}))
        {:error {:message "Only merchant creator can add branches!"}}))
    {:error {:message "Unauthorized operation not permitted"}}))

(defn update-branches?
  [token branches-name address reward-rates asset-code minimum-spend approval-code is-active]

  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (if (= created-by (get (merchant/get-merchants-by-owner conn/db {:CREATED_BY created-by}) :created_by))
        (let [branches-id (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name}) :id)]
          (try
            (branches/update-branches conn/db {:ID branches-id
                                               :ADDRESS address
                                               :REWARD_RATES reward-rates
                                               :ASSET_CODE  asset-code
                                               :MINIMUM_SPEND minimum-spend
                                               :APPROVAL_CODE approval-code
                                               :IS_ACTIVE is-active})
            {:message "Successfully updated branches"}
            (catch Exception ex
              (writelog/op-log! (str "ERROR : " (.getMessage ex)))
              {:error {:message "Something went wrong on our end"}})))
        {:error {:message "Only merchant creator can update branches!"}}))
    {:error {:message "Unauthorized operation not permitted"}}))