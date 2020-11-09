-- :name create-trxarchive-table :! 
CREATE TABLE IF NOT EXISTS TRXARCHIVE (
    ID VARCHAR (36) PRIMARY KEY,
    BLOCK NUMERIC,
    HASH VARCHAR (66),
    SENDER VARCHAR (48),
    DESTINATION VARCHAR (48),
    AMOUNT NUMERIC,
    FEE  NUMERIC,
    MEMO TEXT,
    CREATED_AT timestamp NOT NULL default current_timestamp,
    CREATED_BY VARCHAR (36),
    UPDATED_BY VARCHAR (36) 
)

-- :name drop-trxarchive-table :!
DROP TABLE IF EXISTS TRXARCHIVE

-- :name add-trxarchive :! :n 
INSERT INTO TRXARCHIVE (
    ID,
    BLOCK,
    HASH,
    SENDER,
    DESTINATION,
    AMOUNT,
    FEE,
    MEMO,
    CREATED_BY
)
VALUES (
    :ID,
    :BLOCK,
    :HASH,
    :SENDER,
    :DESTINATION,
    :AMOUNT,
    :FEE,
    :MEMO,
    :CREATED_BY
);

--:name get-all-trxarchive :? :*
SELECT * FROM TRXARCHIVE;

-- :name get-trx-by-hash :? :* 
SELECT * FROM TRXARCHIVE
WHERE HASH = :HASH

-- :name get-trx-by-account :? :* 
SELECT * FROM TRXARCHIVE 
WHERE SENDER = :SENDER 
OR DESTINATION = :DESTINATION;
