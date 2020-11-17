(ns zeetomic-core.db.sdm.sdm-payment-options
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_payment_options.sql")