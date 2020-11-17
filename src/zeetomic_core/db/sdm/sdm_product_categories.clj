(ns zeetomic-core.db.sdm.sdm-product-categories
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_product_categories.sql")