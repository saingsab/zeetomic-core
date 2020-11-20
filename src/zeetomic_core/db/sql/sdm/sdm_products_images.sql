-- :name create-sdm-products-images-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_PRODUCTS_IMAGES(
    ID	        VARCHAR (36) PRIMARY KEY,
    URL	        VARCHAR,
    PRODUCT_ID	VARCHAR (36),
    CREATED_AT	TIMESTAMP NOT NULL default current_timestamp,
    CREATED_BY	VARCHAR (36),
    UPDATED_BY	VARCHAR (36),
    UPDATED_AT	TIMESTAMP
);

-- :name create-sdm-products-images-table :!
DROP TABLE IF EXISTS SDM_PRODUCTS_IMAGES;

-- :name add-sdm-products-images :! :n 
INSERT INTO SDM_PRODUCTS_IMAGES (ID, URL, PRODUCT_ID, CREATED_BY)
VALUES (:ID, :URL, :PRODUCT_ID, :CREATED_BY);

-- :name get-sdm-products-images-by-product-id :? :*
SELECT * FROM SDM_PRODUCTS_IMAGES
WHERE PRODUCT_ID = :PRODUCT_ID;
