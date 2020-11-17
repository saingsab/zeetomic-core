-- :name create-sdm-shipping-services-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_SHIPPING_SERVICES(
    ID	                VARCHAR (36) PRIMARY KEY,
    SHIPPING_SERVICE	TEXT,
    PRICE	            NUMERIC
);

-- :name drop-sdm-shipping-services-table :!
DROP TABLE IF EXISTS SDM_SHIPPING_SERVICES;

-- :name get-sdm-shipping-services :? :*
SELECT * FROM SDM_SHIPPING_SERVICES;

-- :name get-sdm-shipping-services-by-id :? :1
SELECT * FROM SDM_SHIPPING_SERVICES
WHERE ID = :ID;

-- ------DATA------
-- SW Logistic
-- 1.99$
-- INSERT INTO SDM_SHIPPING_SERVICES (ID, SHIPPING_SERVICE, PRICE)
-- VALUES ('b8fd8a60-242c-405d-8a62-1ae2880094a6', 'SW Logistic', 1.99)