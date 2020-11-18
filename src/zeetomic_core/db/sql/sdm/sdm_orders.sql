-- :name create-sdm-orders-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_ORDERS(
    ID	                VARCHAR (36) PRIMARY KEY,
    PRODUCT_ID	        VARCHAR (36),
    QAUANTITY	        NUMERIC,
    SHIPPING_ADDRESS    TEXT,
    BUYER_ID	        VARCHAR (36),
    TOTAL	            NUMERIC,
    STATUS_ID	        VARCHAR (36),
    CREATED_AT	        TIMESTAMP NOT NULL default current_timestamp,
    CREATED_BY	        VARCHAR (36),
    UPDATED_BY	        VARCHAR (36),
    UPDATED_AT	        TIMESTAMP
);

-- :name drop-sdm-orders-table :!
DROP TABLE IF EXISTS SDM_ORDERS;

-- :name make-orders :! :n
INSERT INTO SDM_ORDERS( ID,
                        PRODUCT_ID,
                        QAUANTITY,
                        SHIPPING_ADDRESS,
                        BUYER_ID,
                        TOTAL,
                        STATUS_ID,
                        CREATED_BY
)
VALUES (:ID,
        :PRODUCT_ID,
        :QAUANTITY,
        :SHIPPING_ADDRESS,
        :BUYER_ID,
        :TOTAL,
        :STATUS_ID,
        :CREATED_BY
);

-- :name get-orders-by-seller :? :1
SELECT * FROM SDM_ORDERS 
WHERE CREATED_BY = :CREATED_BY;

-- :name get-orders-by-buyer :? :1
SELECT * FROM SDM_ORDERS 
WHERE CREATED_BY = :CREATED_BY;

-- :name get-orders-by-id :? :1
SELECT * FROM SDM_ORDERS 
WHERE ID = :ID;

-- :name update-orders-status :! :n
UPDATE SDM_ORDERS
SET STATUS_ID  = :STATUS_ID
WHERE ID = :ID