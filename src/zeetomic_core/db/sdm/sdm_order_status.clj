(ns zeetomic-core.db.sdm.sdm-order-status
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_order_status.sql")