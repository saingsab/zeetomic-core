(ns zeetomic-core.db.sdm.sdm-shipping-services
    (:require [hugsql.core :as hugsql]))
  
  (hugsql/def-db-fns "zeetomic_core/db/sql/sdm/sdm_shipping_services.sql")