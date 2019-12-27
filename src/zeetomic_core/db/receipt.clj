(ns zeetomic-core.db.receipt
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/receipt.sql")