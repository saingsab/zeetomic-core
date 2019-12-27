-- :name create-documenttype-table :! 
CREATE TABLE IF NOT EXISTS DOCUMENTTYPE (
    ID VARCHAR (36) PRIMARY KEY,
    DOCUMENT_NAME VARCHAR (15),
    CREATED_AT timestamp NOT NULL default current_timestamp,
    UPDATED_AT timestamp
)

-- :name drop-documenttype-table :!
DROP TABLE IF EXISTS DOCUMENTTYPE

-- :name insert-documenttype-data :! :n 
INSERT INTO DOCUMENTTYPE (ID, DOCUMENT_NAME)
VALUES  ('819fb558-1d8e-4254-ab0a-a65cc509b8ca', 'National ID'),
        ('c9b1552c-6424-4ce6-87d4-f45c8a3edc7a', 'Passport'),
        ('92f63b80-5a3c-486d-b41f-bc6983649b93', 'Driver License')
        

-- :name get-documenttype-by-name :? :1
SELECT * FROM DOCUMENTTYPE 
WHERE DOCUMENT_NAME = :DOCUMENT_NAME

-- :name get-all-documenttype :? :*
SELECT * FROM DOCUMENTTYPE