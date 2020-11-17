(ns zeetomic-core.db.sdm.sdm-products-images
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_products_images.sql")