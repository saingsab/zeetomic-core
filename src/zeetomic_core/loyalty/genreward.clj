(ns zeetomic-core.loyalty.genreward
  (:require [zeetomic-core.db.branches :as branches]
            [zeetomic-core.db.merchant :as merchant]
            [zeetomic-core.db.receipt :as receipt]
            [zeetomic-core.db.hashval :as hashval]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.operation.pay :as pay]
            [zeetomic-core.util.ed :as ed]
            [clojure.data.json :as json]
            [zeetomic-core.util.writelog :as writelog]
            [ring.util.http-response :refer :all]))

(def txid (atom ""))

(defn reward
  [branches-name amount]
  (let [reward-rates (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME branches-name}) :reward_rates)]
    (* amount reward-rates)))

(defn update-str!
  ;; Change the hast to invalid
  [id update-by]
  (try
    (hashval/update-hashval conn/db {:ID id :UPDATED_BY update-by})
    (catch Exception ex
      (writelog/op-log! (str "ERROR : update-str " (.getMessage ex))))))

(defn gen-str
  [token location approval-code receipt-no amount]
  (if (= (auth/authorized? token) true)
    (if (= approval-code (get (branches/get-branches-by-name conn/db {:BRANCHES_NAME location}) :approval_code))
      (let [created-by (get (auth/token? token) :_id)]
        (try
          (println created-by)
          (reset! txid (java.util.UUID/randomUUID))
        ;; save receipt to display on activity on client
          (receipt/add-receipt conn/db {:ID @txid
                                        :RECEIPT_NO receipt-no
                                        :AMOUNT (Float. amount)
                                        :LOCATION location
                                        :REWARDS (reward location (Float. amount))
                                        :STATUS "Pendding"
                                        :CREATED_BY created-by})
          (println "Saved reciept")
        ;; Save hash data into the table
          (hashval/add-hashval conn/db {:ID @txid
                                        :HASHS (ed/encrypt (json/json-str {:receipts-id (str @txid) :branches-name location :amount (reward location (Float. amount))}))
                                        :CREATED_BY created-by})
        ;; Return sting to generator
          (ok {:message (ed/encrypt (json/json-str {:receipts-id (str @txid) :branches-name location :amount (reward location (Float. amount))}))})

          (catch Exception ex
            (writelog/op-log! (str "ERROR : gen-str " (.getMessage ex)))
            (ok {:error {:message "Something went wrong on our end"}}))))
      (ok {:error {:message "The approval code was not correct!"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn rewarding!
  [receipts-id branches-name amount wallet updated-by]
  (try
    ;; Future function
    (future (Thread/sleep 3000)
            (try
              ;; pay to wallet 
              (println (str "start paying the wallet :" wallet))
              (pay/reward! branches-name wallet amount receipts-id)
              ;; Update the database update-by and status to finish
              (receipt/update-receipt-status conn/db {:ID receipts-id :UPDATED_BY updated-by})
              (catch Exception ex
                (writelog/op-log! (str "ERROR : rewarding!" (.getMessage ex))))))
    (ok {:message (str "Congratulations! you received " amount " Token")})
    (catch Exception ex
      (writelog/op-log! (str "ERROR : " (.getMessage ex)))
      (ok {:error {:message "Something went wrong on our end"}}))))

(defn valid-str?
  [token hashs]
  (if (= (auth/authorized? token) true)
    (try
      (let [id (get (json/read-str (ed/decrypt hashs) :key-fn keyword) :receipts-id)]
        ;; If QR valid 
        (if (get (hashval/get-hashval-by-id conn/db {:ID id}) :is_valid)
          (try
            ;; Startpaying 
            (future (Thread/sleep 3000)
                    (pay/reward! (get (json/read-str (ed/decrypt hashs) :key-fn keyword) :branches-name)
                                 (get (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}) :wallet)
                                ;  Forgive me this not a best solution yet.
                                 (str (Float. (get (json/read-str (ed/decrypt hashs) :key-fn keyword) :amount)))))

            ;; Update Receipt
            (receipt/update-receipt-status conn/db {:ID id :UPDATED_BY (get (auth/token? token) :_id)})
            (update-str! id (get (auth/token? token) :_id))
            (ok {:message (str "Congratulations! you received " (Float. (get (json/read-str (ed/decrypt hashs) :key-fn keyword) :amount))  " Token")})
            (catch Exception ex
              (writelog/op-log! (str "ERROR : " (.getMessage ex)))
              (ok {:error {:message "Something went wrong on our end"}})))
          ;; QR already expired!
          (ok {:error {:message "Your QR code is not valid or already expired"}})))

      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))
