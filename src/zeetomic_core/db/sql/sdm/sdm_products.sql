-- :name create-sdm-products-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_PRODUCTS(
    ID	        VARCHAR (36) PRIMARY KEY,
    NAME	    TEXT,
    PRICE	    NUMERIC,
    SHIPPING	VARCHAR (36),
    WEIGHT	    VARCHAR (36), 
    DESCRIPTION	VARCHAR,
    THUMBNAIL	VARCHAR, 
    CATEGORY_ID	VARCHAR (36),
    PAYMENT_ID	VARCHAR (36),
    IS_SOLD     BOOLEAN NOT NULL default FALSE,
    CREATED_AT	TIMESTAMP NOT NULL default current_timestamp,
    CREATED_BY	VARCHAR (36),
    UPDATED_BY	VARCHAR (36),
    UPDATED_AT	TIMESTAMP
);

-- :name drop-sdm-products-table :!
DROP TABLE IF EXISTS SDM_PRODUCTS

-- :name add-products :! :n
INSERT INTO SDM_PRODUCTS (ID, 
                          NAME, 
                          PRICE,
                          SHIPPING,
                          WEIGHT,
                          DESCRIPTION,
                          THUMBNAIL,
                          CATEGORY_ID,
                          PAYMENT_ID,
                          CREATED_BY
)
VALUES (:ID, 
        :NAME, 
        :PRICE,
        :SHIPPING,
        :WEIGHT,
        :DESCRIPTION,
        :THUMBNAIL,
        :CATEGORY_ID,
        :PAYMENT_ID,
        :CREATED_BY
);

-- :name get-all-products :? :*
SELECT  p.ID,
        p.NAME,
        p.PRICE,
        s.SHIPPING_SERVICE,
        s.PRICE AS SHIPPING_FEE,
        p.WEIGHT,
        p.DESCRIPTION,
        p.THUMBNAIL,
        c.CATEGORY_NAME,
        p.PAYMENT_ID,
        p.IS_SOLD,
        p.CREATED_AT,
        (u.FIRST_NAME, u.LAST_NAME) AS SELLER,
        u.PHONENUMBER,
        u.ADDRESS

FROM SDM_PRODUCTS AS p
INNER JOIN USERS AS u
ON u.ID = p.CREATED_BY
INNER JOIN SDM_PRODUCT_CATEGORY AS c
ON p.CATEGORY_ID = c.ID
INNER JOIN SDM_SHIPPING_SERVICES AS s
ON p.SHIPPING = s.ID
ORDER BY CREATED_AT DESC;

-- :name get-all-products-gallery :? :*
SELECT p.ID,
       p.NAME,
       r.URL
FROM SDM_PRODUCTS AS p 
INNER JOIN SDM_PRODUCTS_IMAGES AS r
ON p.ID=r.PRODUCT_ID
WHERE p.ID = 'f6213a56-d479-4024-b1eb-d6879c4a6068'
ORDER BY p.CREATED_AT DESC;

-- :name get-products-by-owner :? :*
SELECT * FROM SDM_PRODUCTS 
WHERE CREATED_BY = :CREATED_BY
ORDER BY CREATED_AT DESC;

-- :name get-products-by-id :? :1
SELECT * FROM SDM_PRODUCTS 
WHERE ID = :ID;

-- :name set-products-to-sold :! :n
UPDATE SDM_PRODUCTS
SET IS_SOLD  = TRUE
WHERE ID = :ID