-- :name create-sdm-product-categories-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_PRODUCT_CATEGORY(
    ID	            VARCHAR (36) PRIMARY KEY,
    CATEGORY_NAME	TEXT,
    CREATED_AT	    TIMESTAMP NOT NULL default current_timestamp,
    CREATED_BY	    VARCHAR (36),
    UPDATED_BY	    VARCHAR (36),
    UPDATED_AT	    TIMESTAMP
);

-- :name create-sdm-product-categories-table :!
DROP TABLE IF EXISTS SDM_PRODUCT_CATEGORY;

-- :name add-status-data :! :n 
INSERT INTO SDM_PRODUCT_CATEGORY (ID, CATEGORY_NAME)
VALUES  ('504e7e78-4c63-4d00-959b-55509a1a06f8', 'Cereal'),
        ('e285725c-3958-4700-a751-f5e57e600a16', 'Vegetable'),
        ('1a8bdfc5-a11a-42dd-b7e5-7d36cc605be4', 'Fish'),
        ('4e984edb-abd2-4691-990f-a6b1413cf472', 'Meat'),
        ('8ea6a9d8-31d3-48a1-b0ee-68a1e181b576', 'Fruit'),
        ('1f8ccf75-352b-4298-b57a-d08cb2223a7c', 'Others');

-- :name get-sdm-product-categories :? :*
SELECT * FROM SDM_PRODUCT_CATEGORY;