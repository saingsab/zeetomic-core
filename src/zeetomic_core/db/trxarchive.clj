(ns zeetomic-core.db.trxarchive
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/trxarchive.sql")