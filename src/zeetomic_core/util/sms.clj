(ns zeetomic-core.util.mailling
    (:require [clj-http.client :as client]
              [aero.core :refer (read-config)]
              [zeetomic-core.util.writelog :as writelog]))

(defn send-sms [phone]
    (client/post ))