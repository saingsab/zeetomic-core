(ns zeetomic-core.db.hashval
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/hashval.sql")