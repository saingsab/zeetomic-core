-- :name create-status-table :! 
CREATE TABLE IF NOT EXISTS STATUS (
    ID VARCHAR (36) PRIMARY KEY,
    STATUS_NAME VARCHAR (10),
    CREATED_AT timestamp NOT NULL default current_timestamp,
    UPDATED_AT timestamp
)

-- :name drop-status-table :!
DROP TABLE IF EXISTS STATUS

-- :name insert-status-data :! :n 
INSERT INTO STATUS (ID, STATUS_NAME)
VALUES  ('cb6fcc67-0c03-405c-9426-ce1685208f68', 'inactive'),
        ('a1ca5b7b-d15c-44a3-9c06-c32793a16b89', 'active'),
        ('7acb0fc7-c873-4b1a-83ad-3942e57bb151', 'verifying'),
        ('17d67b96-3f29-4320-a945-b1fd86ff5486', 'verified'),
        ('aeb6c1a1-fd82-4a30-8520-29d3d9435375', 'disabled')

-- :name get-status-by-name :? :1
SELECT * FROM STATUS 
WHERE STATUS_NAME = :STATUS_NAME