(ns zeetomic-core.db.sdm.sdm-products
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_products.sql")