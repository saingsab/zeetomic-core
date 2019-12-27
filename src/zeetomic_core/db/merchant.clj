(ns zeetomic-core.db.merchant
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/merchant.sql")