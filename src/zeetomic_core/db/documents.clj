(ns zeetomic-core.db.documents
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "zeetomic_core/db/sql/documents.sql")