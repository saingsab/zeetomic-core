(ns zeetomic-core.db.branches
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/branches.sql")