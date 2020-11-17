(ns zeetomic-core.db.sdm.sdm-orders
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_orders.sql")