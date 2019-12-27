(ns zeetomic-core.db.users
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/users.sql")