-- :name create-apiacc-table :! 
CREATE TABLE IF NOT EXISTS APIACC (
    ID VARCHAR (36) PRIMARY KEY,
    APIKEY VARCHAR (56),
    APISEC TEXT,
    IS_ACTIVE BOOLEAN NOT NULL default TRUE,
    CREATED_AT TIMESTAMP NOT NULL default current_timestamp,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR (36)
);

-- :name drop-apiacc-table :!
DROP TABLE IF EXISTS APIACC

-- :name set-apikey :! :n 
INSERT INTO APIACC (ID, APIKEY, APISEC)
VALUES (:ID, :APIKEY, :APISEC)

-- :name get-api-by-id :? :1
SELECT * FROM APIACC 
WHERE APIKEY = :APIKEY