-- :name create-receipt-table :! 
CREATE TABLE IF NOT EXISTS RECEIPTS (
    ID VARCHAR (36) PRIMARY KEY,
    RECEIPT_NO VARCHAR (36),
    AMOUNT NUMERIC,
    LOCATION TEXT,
    REWARDS NUMERIC,
    REMARK TEXT,
    STATUS VARCHAR (36),
    CREATED_AT timestamp NOT NULL default current_timestamp,
    CREATED_BY VARCHAR (36)
)

-- :name drop-receipt-table :!
DROP TABLE IF EXISTS RECEIPTS

-- :name add-receipt :! :n 
INSERT INTO RECEIPTS (
    ID, 
    RECEIPT_NO, 
    AMOUNT, 
    LOCATION, 
    REWARDS, 
    STATUS, 
    CREATED_BY
)
VALUES (
    :ID, 
    :RECEIPT_NO,
    :AMOUNT,
    :LOCATION,
    :REWARDS,
    :STATUS,
    :CREATED_BY
)

-- :name update-receipt-status :! :n
UPDATE RECEIPTS
SET STATUS  = :STATUS
WHERE ID = :ID

--:name get-all-receipt :? :*
SELECT * FROM RECEIPTS

-- :name get-receipt-by-owner :? :1
SELECT * FROM RECEIPTS
WHERE CREATED_BY = :CREATED_BY