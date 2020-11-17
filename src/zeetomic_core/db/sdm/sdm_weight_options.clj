
(ns zeetomic-core.db.sdm.sdm-weight-options
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_weight_options.sql")