-- :name create-sdm-orders-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
-- :doc Modified at 20201202 by saing t.me/saingsab 'Adding Order status'
CREATE TABLE IF NOT EXISTS SDM_ORDERS(
    ID	                VARCHAR (36) PRIMARY KEY,
    PRODUCT_ID	  VARCHAR (36),
    QAUANTITY	         NUMERIC,
    SHIPPING_ADDRESS   TEXT,
    BUYER_ID	         VARCHAR (36),
    TOTAL	         NUMERIC,
    STATUS_ID	         VARCHAR (36),
    CREATED_AT	  TIMESTAMP NOT NULL default current_timestamp,
    CREATED_BY	  VARCHAR (36),
    UPDATED_BY	  VARCHAR (36),
    UPDATED_AT	  TIMESTAMP
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

-- :name get-orders-by-seller :? :*
SELECT o.ID, 
       p.NAME,
       p.PRICE,
       o.QAUANTITY,
       s.SHIPPING_SERVICE, 
       o.SHIPPING_ADDRESS, 
       u.PHONENUMBER AS BUYER_PHONENUMBER,
       (u.FIRST_NAME, u.LAST_NAME) AS BUYER, 
       o.TOTAL,
       p.THUMBNAIL,
       os.ORDER_STATUS_DEC,
       o.PRODUCT_ID

FROM SDM_PRODUCTS as p
INNER JOIN  SDM_ORDERS as o
ON o.PRODUCT_ID=p.ID
INNER JOIN SDM_SHIPPING_SERVICES AS s
ON p.SHIPPING = s.ID
INNER JOIN SDM_ORDER_STATUS AS os
ON o.STATUS_ID = os.ID
INNER JOIN USERS as u
ON u.ID = o.CREATED_BY
WHERE p.CREATED_BY = :CREATED_BY
ORDER BY o.CREATED_AT DESC;

-- :name get-orders-by-buyer :? :*
SELECT o.ID, 
       p.NAME,
       p.PRICE,
       o.QAUANTITY,
       s.SHIPPING_SERVICE, 
       o.SHIPPING_ADDRESS, 
       o.BUYER_ID, 
       o.TOTAL,
       u.PHONENUMBER AS SELLER_PHONENUMBER,
       (u.FIRST_NAME, u.LAST_NAME) AS SELLER,
       p.THUMBNAIL,
       os.ORDER_STATUS_DEC,
       o.PRODUCT_ID

FROM SDM_PRODUCTS as p
INNER JOIN  SDM_ORDERS as o
ON o.PRODUCT_ID=p.ID
INNER JOIN SDM_SHIPPING_SERVICES AS s
ON p.SHIPPING = s.ID
INNER JOIN SDM_ORDER_STATUS AS os
ON o.STATUS_ID = os.ID
INNER JOIN USERS as u
ON u.ID = p.CREATED_BY
WHERE o.CREATED_BY = :CREATED_BY
ORDER BY o.CREATED_AT DESC;

-- :name get-orders-by-id :? :1
SELECT * FROM SDM_ORDERS 
WHERE ID = :ID;

-- :name update-orders-status :! :n
UPDATE SDM_ORDERS
SET STATUS_ID  = :STATUS_ID
WHERE ID = :ID