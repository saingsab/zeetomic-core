(ns zeetomic-core.loyalty.branches
  (:require [zeetomic-core.db.branches :as branches]
            [zeetomic-core.db.merchant :as merchant]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all]))

(defn add-branches!
  [token merchant-id branches-name address reward-rates asset-code minimum-spend approval-code logo-uri]
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
                                          :LOGO_URI logo-uri
                                          :CREATED_BY created-by})
          (ok {:message "Successfully added branches"})
          (catch Exception ex
            (writelog/op-log! (str "ERROR : " (.getMessage ex)))
            (ok {:error {:message "Something went wrong on our end"}})))
        (ok {:error {:message "Only merchant creator can add branches!"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn update-branches?
  [token branches-name address reward-rates asset-code minimum-spend approval-code is-active logo-uri]

  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (if (= created-by (get (merchant/get-merchants-by-owner conn/db {:CREATED_BY created-by}) :created_by))
        (let [branches-id (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name}) :id)]
          (try
            (branches/update-branches conn/db {:ID branches-id
                                               :ADDRESS address
                                               :REWARD_RATES (Float. reward-rates)
                                               :ASSET_CODE  asset-code
                                               :MINIMUM_SPEND (Float. minimum-spend)
                                               :APPROVAL_CODE approval-code
                                               :LOGO_URI logo-uri
                                               :IS_ACTIVE is-active})
            (ok {:message "Successfully updated branches"})
            (catch Exception ex
              (writelog/op-log! (str "ERROR : " (.getMessage ex)))
              (ok {:error {:message "Something went wrong on our end"}}))))
        (ok {:error {:message "Only merchant creator can update branches!"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))


(defn list-all-branches!
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (branches/list-all-branches conn/db))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn granted-user!
  [token branches-name email]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (if (= created-by (get (merchant/get-merchants-by-owner conn/db {:CREATED_BY created-by}) :created_by))
        (try
          (branches/granted-to conn/db {:BRANCHES_NAME branches-name :GRANTED_FOR (get (users/get-users-by-mail conn/db {:EMAIL email}) :id)})
          (ok {:message (str "Successfully granted " email " To access" branches-name)})
          (catch Exception ex
            (writelog/op-log! (str "ERROR : FN granted-user! " (.getMessage ex)))
            (ok {:error {:message "Something went wrong on our end"}})))
        (ok {:error {:message "Only merchant creator can update branches!"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-branches-by-granted
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (branches/list-branches-by-granted conn/db {:GRANTED_FOR (get (auth/token? token) :_id)}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : get-branches-by-granted " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))