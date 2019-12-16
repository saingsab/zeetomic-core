(ns zeetomic-core.util.disconn
  (:require [monger.core :as mg]))

(let [conn (mg/connect)]
  (mg/disconnect conn))