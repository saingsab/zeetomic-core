-- :name create-merchant-table :! 
CREATE TABLE IF NOT EXISTS MERCHANTS (
    ID VARCHAR (36) PRIMARY KEY,
    MERCHANT_NAME VARCHAR (50) UNIQUE,
    SHORTNAME VARCHAR (50) UNIQUE,
    CREATED_AT timestamp NOT NULL default current_timestamp,
    UPDATED_AT timestamp,
    CREATED_BY VARCHAR (36)
)

-- :name drop-merchants-table :!
DROP TABLE IF EXISTS MERCHANTS

-- :name add-merchants :! :n 
INSERT INTO MERCHANTS (ID, MERCHANT_NAME, SHORTNAME, CREATED_BY)
VALUES (:ID, :MERCHANT_NAME, :SHORTNAME, :CREATED_BY)

-- :name update-merchants :! :n
UPDATE MERCHANTS
SET MERCHANT_NAME   = :MERCHANT_NAME,
    SHORTNAME       = :SHORTNAME
where ID = :ID

--:name get-all-merchants :? :*
SELECT * FROM MERCHANTS

-- :name get-merchants-by-owner :? :1
SELECT * FROM MERCHANTS
WHERE CREATED_BY = :CREATED_BY

-- :name get-merchants-by-name :? :1
SELECT * FROM MERCHANTS
WHERE MERCHANT_NAME = :MERCHANT_NAME