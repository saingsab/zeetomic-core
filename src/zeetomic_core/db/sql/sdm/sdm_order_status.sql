-- :name create-sdm-order-status-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_ORDER_STATUS(
    ID	                VARCHAR (36) PRIMARY KEY,
    ORDER_STATUS_DEC	TEXT
);

-- :name create-sdm-order-status-table :!
DROP TABLE IF EXISTS SDM_ORDER_STATUS;

-- :name insert-status-data :! :n 
INSERT INTO SDM_ORDER_STATUS (ID, ORDER_STATUS_DEC)
VALUES  ('504e7e78-4c63-4d00-959b-55509a1a06f8', 'Place Order'),
        ('e285725c-3958-4700-a751-f5e57e600a16', 'Pay Success'),
        ('1a8bdfc5-a11a-42dd-b7e5-7d36cc605be4', 'Shipment'),
        ('06eb36d5-8a81-477a-b4c1-2e72566559cc', 'Order Complete');

-- :name get-sdm-order-status :? :*
SELECT * FROM SDM_ORDER_STATUS;

-- :name get-sdm-order-status-by-dec :? :1
SELECT * FROM SDM_ORDER_STATUS 
WHERE ORDER_STATUS_DEC = :ORDER_STATUS_DEC;