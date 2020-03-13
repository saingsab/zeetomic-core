-- :name create-receipt-table :! 
CREATE TABLE IF NOT EXISTS HASHVAL (
    ID VARCHAR (36) PRIMARY KEY,
    HASHS TEXT,
    IS_VALID BOOLEAN NOT NULL default TRUE,
    CREATED_AT timestamp NOT NULL default current_timestamp,
    CREATED_BY VARCHAR (36),
    UPDATED_BY VARCHAR (36) 
)

-- :name drop-receipt-table :!
DROP TABLE IF EXISTS HASHVAL

-- :name add-hashval :! :n 
INSERT INTO HASHVAL (
    ID, 
    HASHS, 
    CREATED_BY
)
VALUES (
    :ID, 
    :HASHS,
    :CREATED_BY
)

-- :name update-hashval :! :n
UPDATE HASHVAL
SET IS_VALID  = FALSE,
    UPDATED_BY = :UPDATED_BY
WHERE ID = :ID

-- :name get-hashval-by-id :? :1
SELECT * FROM HASHVAL
WHERE ID = :ID