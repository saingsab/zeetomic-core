(ns zeetomic_core.sdm.sdm-orders
  (:require [zeetomic-core.db.sdm.sdm-orders :as sdm-orders]
            [zeetomic-core.db.sdm.sdm-products :as sdm-products]
            [zeetomic-core.db.sdm.sdm-shipping-services :as sdm-shipping-services]
            [zeetomic-core.db.sdm.sdm-order-status :as sdm-order-status]
            [zeetomic-core.util.writelog :as writelog]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.util.conn :as conn]
            [ring.util.http-response :refer :all]))

(def txid (atom ""))

;products price + shipping cost
(defn total-cost 
    [product-id]
    (+ (get (sdm-products/get-products-by-id conn/db {:ID product-id}) :price)
       (get (sdm-shipping-services/get-sdm-shipping-services-by-id conn/db {:ID (get (sdm-products/get-products-by-id conn/db {:ID product-id}) :shipping)}) :price)))

(defn status-id 
    [dec]
    (get (sdm-order-status/get-sdm-order-status-by-dec conn/db {:ORDER_STATUS_DEC dec}) :id))

(defn make-orders 
  [token product-id qty shipping-address]
  (if (= (auth/authorized? token) true)
    (let [created-by (get (auth/token? token) :_id)]
      (try 
        (reset! txid (java.util.UUID/randomUUID))
        (sdm-orders/make-orders conn/db {:ID @txid
                                         :PRODUCT_ID product-id 
                                         :QAUANTITY qty 
                                         :SHIPPING_ADDRESS shipping-address 
                                         :BUYER_ID created-by 
                                         :TOTAL (Float/parseFloat (total-cost product-id))
                                         :STATUS_ID (status-id "Place Order")
                                         :CREATED_BY created-by})
        ;[todo]
        ; Payment deduct from wallet.
        (ok {:message "The order has been placed"})
    (catch Exception ex
        (writelog/op-log! (str "ERROR : FN make-orders " (.getMessage ex)))
        {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

; Update the ordered status by products owner only
; Accept Payment
; Triger SMS or Email
; Pay Success
(defn update-order-success-pay
    [token order-id]
    (if (= (auth/authorized? token) true)
        ; Only seller can update into seccess payment
        (if (= (get (sdm-products/get-products-by-id conn/db {:ID (get (sdm-orders/get-orders-by-id conn/db {:ID order-id}) :product_id)}) :created_by)
               (get (sdm-orders/get-orders-by-id conn/db {:ID order-id}) :created_by))
            ; True lets product owner change order status
            (try
                (sdm-orders/update-orders-status conn/db {:ID order-id :STATUS_ID (status-id "Pay Success")})
                (ok {:message "Order successfully paid"})
            (catch Exception ex
                (writelog/op-log! (str "ERROR : FN update-order-success-pay " (.getMessage ex)))
                {:error {:message "Internal server error"}}))
                ; False  
        (ok {:error {:message "Sorry! Only the seller can update this operation"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

; Shipment
(defn update-order-shipment
    [token order-id]
    (if (= (auth/authorized? token) true)
        ; Only seller can update into seccess payment
        (if (= (get (sdm-products/get-products-by-id conn/db {:ID (get (sdm-orders/get-orders-by-id conn/db {:ID order-id}) :product_id)}) :created_by)
               (get (sdm-orders/get-orders-by-id conn/db {:ID order-id}) :created_by))
            ; True lets product owner change order status
            (try
                (sdm-orders/update-orders-status conn/db {:ID order-id :STATUS_ID (status-id "Shipment")})
                (ok {:message "Order started shipping"})
            (catch Exception ex
                (writelog/op-log! (str "ERROR : FN update-order-success-pay " (.getMessage ex)))
                {:error {:message "Internal server error"}}))
                ; False  
        (ok {:error {:message "Sorry! Only the seller can update this operation"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

; Get Goods
; The buyer update 
; Triger SMS or Email
; Order Complete
(defn update-order-completed
    [token order-id]
    (if (= (auth/authorized? token) true)
        ; Only buyer can update into seccess payment
        (if (= (get (auth/token? token) :_id)
               (get (sdm-orders/get-orders-by-id conn/db {:ID order-id}) :created_by))
            ; True lets product owner change order status
            (try
                (sdm-orders/update-orders-status conn/db {:ID order-id :STATUS_ID (status-id "Order Complete")})
                ; mark products is sold
                (sdm-products/set-products-to-sold conn/db {:ID (get (sdm-orders/get-orders-by-id conn/db {:ID order-id}) :product_id)})
                (ok {:message "Order successfully completed"})
            (catch Exception ex
                (writelog/op-log! (str "ERROR : FN update-order-success-pay " (.getMessage ex)))
                {:error {:message "Internal server error"}}))
                ; False  
        (ok {:error {:message "Sorry! Only the buyer can update this operation"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn list-order 
    [token]
    (if (= (auth/authorized? token) true)
      (let [created-by (get (auth/token? token) :_id)]
        (try
          (ok (sdm-orders/get-orders-by-buyer conn/db {:CREATED_BY created-by}))
      (catch Exception ex
          (writelog/op-log! (str "ERROR : FN get-users-by-owner " (.getMessage ex)))
          {:error {:message "Internal server error"}})))
  (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

; [todo]
; list by difference status

