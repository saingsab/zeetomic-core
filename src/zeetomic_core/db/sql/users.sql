-- :name create-users-table :! 
CREATE TABLE IF NOT EXISTS USERS (
    ID VARCHAR (36) PRIMARY KEY,
    FIRST_NAME VARCHAR (50),
    MID_NAME VARCHAR (50),
    LAST_NAME VARCHAR (50),
    DESCRIPTION text,
    EMAIL   VARCHAR (50) UNIQUE,
    GENDER VARCHAR (1),
    PROFILE_IMG text,
    WALLET VARCHAR (56) UNIQUE,
    SEED VARCHAR (256),
    PASSWORD VARCHAR (256) NOT NULL,
    TEMP_TOKEN text,
    PIN VARCHAR (256),
    USER_STATUS VARCHAR (36),
    IS_PARTNER BOOLEAN NOT NULL default FALSE,
    NATIONALITY VARCHAR (50),
    OCCUPATION VARCHAR (50),
    PHONENUMBER VARCHAR (13) UNIQUE,
    DOCUMENTS_ID VARCHAR (36),
    STATUS_ID VARCHAR (36),
    ADDRESS text,
    CREATED_AT timestamp NOT NULL default current_timestamp,
    UPDATED_AT timestamp,
    UPDATED_BY VARCHAR (36)
)

-- :name drop-users-table :!
DROP TABLE IF EXISTS USERS

-- :name register-users-by-phone :! :n 
INSERT INTO USERS (ID, PHONENUMBER, PASSWORD, TEMP_TOKEN, STATUS_ID)
VALUES (:ID, :PHONENUMBER, :PASSWORD, :TEMP_TOKEN, :STATUS_ID)

-- :name register-users-by-mail :! :n 
INSERT INTO USERS (ID, EMAIL, PASSWORD, TEMP_TOKEN, STATUS_ID)
VALUES (:ID, :EMAIL, :PASSWORD, :TEMP_TOKEN, :STATUS_ID)

-- :name get-all-users :? :*
SELECT * FROM USERS

-- :name get-users-by-mail :? :1
SELECT * FROM USERS 
WHERE EMAIL = :EMAIL

-- :name get-users-by-phone :? :1
SELECT * FROM USERS 
WHERE PHONENUMBER = :PHONENUMBER

-- :name get-seed-by-id :? :1
SELECT SEED 
FROM USERS 
WHERE ID = :ID

-- :name get-pin-by-id :? :1
SELECT PIN FROM USERS 
WHERE ID = :ID

-- :name get-users-by-id :? :1
SELECT FIRST_NAME,
       MID_NAME,
       LAST_NAME,
       DESCRIPTION,
       EMAIL,
       GENDER,
       PROFILE_IMG,
       WALLET,
       STATUS_ID,
       IS_PARTNER,
       NATIONALITY,
       OCCUPATION,
       PHONENUMBER,
       DOCUMENTS_ID,
       ADDRESS,
       CREATED_AT
FROM USERS 
WHERE ID = :ID

-- :name get-users-token :? :1
SELECT ID, TEMP_TOKEN
FROM USERS 
WHERE ID = :ID

-- :name get-users-token-phone :? :1
SELECT PHONENUMBER, TEMP_TOKEN
FROM USERS 
WHERE PHONENUMBER = :PHONENUMBER

-- :name user-activation :! :n
UPDATE USERS
SET TEMP_TOKEN = :TEMP_TOKEN,
    STATUS_ID = :STATUS_ID
WHERE ID = :ID

-- :name user-activation-by-phone :! :n
UPDATE USERS
SET TEMP_TOKEN = :TEMP_TOKEN,
    STATUS_ID = :STATUS_ID
WHERE PHONENUMBER = :PHONENUMBER

-- :name update-status :! :n
UPDATE USERS
SET STATUS_ID = :STATUS_ID
where ID = :ID

-- :name setup-user-profile :! :n
-- :doc Update status after setup profile
UPDATE USERS
SET FIRST_NAME  = :FIRST_NAME,
    MID_NAME    = :MID_NAME,
    LAST_NAME   = :LAST_NAME,
    GENDER      = :GENDER,
    STATUS_ID   = :STATUS_ID
WHERE ID = :ID

-- :name setup-user-wallet :! :n
UPDATE USERS
SET WALLET  = :WALLET,
    SEED    = :SEED,
    PIN     = :PIN
WHERE ID    = :ID

-- :name join-partners :! :n
UPDATE USERS
SET IS_PARTNER  = TRUE
WHERE ID    = :ID

-- :name reset-password :! :n
UPDATE USERS
SET PASSWORD = :PASSWORD, 
    TEMP_TOKEN = 0
WHERE PHONENUMBER   = :PHONENUMBER

-- :name set-pin :! :n
UPDATE USERS
SET PIN = :PIN 
WHERE ID   = :ID

-- :name update-temp :! :n
UPDATE USERS
SET TEMP_TOKEN = :TEMP_TOKEN 
WHERE PHONENUMBER   = :PHONENUMBER

-- :name delete-user-by-phone :! :n
DELETE FROM USERS WHERE PHONENUMBER = :PHONENUMBER