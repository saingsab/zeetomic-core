-- :name create-sdm-weight-options-table :! 
-- :doc created at 20201116 by saing t.me/saingsab
CREATE TABLE IF NOT EXISTS SDM_WEIGHT_OPTIONS(
    ID	            VARCHAR (36) PRIMARY KEY,
    WEIGHT_OPTION	TEXT
);

-- :name drop-sdm-weight-options-table :!
DROP TABLE IF EXISTS SDM_WEIGHT_OPTIONS;

-- :name get-sdm-weight-options :? :*
SELECT * FROM SDM_WEIGHT_OPTIONS;


-- ------DATA------
-- KG
-- INSERT INTO SDM_WEIGHT_OPTIONS (ID, WEIGHT_OPTION)
-- VALUES ('b8fd8a60-242c-405d-8a62-1ae2880094a7', 'KG')