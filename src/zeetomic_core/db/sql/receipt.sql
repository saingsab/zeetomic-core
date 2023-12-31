-- :name create-receipt-table :! 
CREATE TABLE IF NOT EXISTS RECEIPTS (
    ID VARCHAR (36) PRIMARY KEY,
    RECEIPT_NO VARCHAR (36),
    AMOUNT NUMERIC,
    LOCATION TEXT,
    IMAGE_URI TEXT,
    REWARDS NUMERIC,
    REMARK TEXT,
    STATUS VARCHAR (36),
    CREATED_AT timestamp NOT NULL default current_timestamp,
    CREATED_BY VARCHAR (36),
    UPDATED_BY VARCHAR (36) 
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
SET STATUS  = 'Completed',
    UPDATED_BY = :UPDATED_BY
WHERE ID = :ID

--:name get-all-receipt :? :*
SELECT * FROM RECEIPTS

-- :name get-receipt-by-owner :? :* 
SELECT * FROM RECEIPTS
WHERE UPDATED_BY = :UPDATED_BY
ORDER BY UPDATED_BY DESC

-- :name transactions-report :? :*
SELECT r.LOCATION, 
       u.FIRST_NAME, 
       u.MID_NAME, 
       u.LAST_NAME, 
       u.EMAIL, 
       u.PHONENUMBER, 
       r.AMOUNT, 
       r.REWARDS, 
       r.CREATED_AT,
       m.CREATED_BY  

FROM RECEIPTS AS r 
INNER JOIN USERS AS u
ON u.ID=r.CREATED_BY
INNER JOIN MERCHANTS AS m
ON u.ID = m.CREATED_BY
WHERE m.CREATED_BY = :CREATED_BY
ORDER BY r.CREATED_AT DESC
LIMIT 15

-- :name trx-from-to-date :? :*
SELECT r.LOCATION, 
       u.FIRST_NAME, 
       u.MID_NAME, 
       u.LAST_NAME, 
       u.EMAIL, 
       u.PHONENUMBER, 
       r.AMOUNT, 
       r.REWARDS, 
       r.CREATED_AT,
       m.CREATED_BY  

FROM RECEIPTS AS r 
INNER JOIN USERS AS u
ON u.ID=r.CREATED_BY
INNER JOIN MERCHANTS AS m
ON u.ID = m.CREATED_BY
WHERE m.CREATED_BY = :CREATED_BY
AND r.CREATED_AT 
BETWEEN TO_TIMESTAMP(:FROM_DATE, 'yyyy-MM-dd HH:mm:ss.SSS') AND TO_TIMESTAMP(:TO_DATE, 'yyyy-MM-dd HH:mm:ss,SSS')
-- BETWEEN :FROM_DATE AND :TO_DATE
ORDER BY r.CREATED_AT DESC


-- :name trx-from-to-date-by-location :? :*
SELECT r.LOCATION, 
       u.FIRST_NAME, 
       u.MID_NAME, 
       u.LAST_NAME, 
       u.EMAIL, 
       u.PHONENUMBER, 
       r.AMOUNT, 
       r.REWARDS, 
       r.CREATED_AT,
       m.CREATED_BY  

FROM RECEIPTS AS r 
INNER JOIN USERS AS u
ON u.ID=r.CREATED_BY
INNER JOIN MERCHANTS AS m
ON u.ID = m.CREATED_BY
WHERE m.CREATED_BY = :CREATED_BY
AND r.CREATED_AT 
BETWEEN TO_TIMESTAMP(:FROM_DATE, 'yyyy-MM-dd HH:mm:ss.SSS') AND TO_TIMESTAMP(:TO_DATE, 'yyyy-MM-dd HH:mm:ss,SSS')
AND r.LOCATION = :LOCATION
ORDER BY r.CREATED_AT DESC

-- :name trx-by-location :? :*
SELECT r.LOCATION, 
       u.FIRST_NAME, 
       u.MID_NAME, 
       u.LAST_NAME, 
       u.EMAIL, 
       u.PHONENUMBER, 
       r.AMOUNT, 
       r.REWARDS, 
       r.CREATED_AT,
       m.CREATED_BY  

FROM RECEIPTS AS r 
INNER JOIN USERS AS u
ON u.ID=r.CREATED_BY
INNER JOIN MERCHANTS AS m
ON u.ID = m.CREATED_BY
WHERE m.CREATED_BY = :CREATED_BY 
AND r.LOCATION = :LOCATION
ORDER BY r.CREATED_AT DESC