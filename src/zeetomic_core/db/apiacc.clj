(ns zeetomic-core.db.apiacc
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/apiacc.sql")