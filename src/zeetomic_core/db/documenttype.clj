(ns zeetomic-core.db.documenttype
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/documenttype.sql")