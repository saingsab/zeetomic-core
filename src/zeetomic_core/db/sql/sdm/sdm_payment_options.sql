-- :name create-sdm-payment-options-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_PAYMENT_OPTIONS(
    ID	            VARCHAR (36) PRIMARY KEY,
    OPTIONS_NAME	TEXT
);

-- :name drop-sdm-payment-options-table :!
DROP TABLE IF EXISTS SDM_PAYMENT_OPTIONS

-- :name insert-status-data :! :n 
INSERT INTO SDM_PAYMENT_OPTIONS (ID, OPTIONS_NAME)
VALUES  ('224f6012-7129-4c07-8738-156e58d30e69', 'Escrow'),
        ('375f4c4b-945d-437e-9a2d-4a0af398f925', 'Direct Payment'),
        ('3802b352-4b1e-4ec3-a24c-c1e042d3fadd', 'Local Bank Transfer');

-- :name get-sdm-payment-options :? :*
SELECT * FROM SDM_PAYMENT_OPTIONS;