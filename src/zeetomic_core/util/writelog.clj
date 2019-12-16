(ns zeetomic-core.util.writelog
  (:require [clojure.java.io :as io]
            [clj-time.core :as t]
            [aero.core :refer (read-config)]))

(def env (read-config ".config.edn"))

(defn tx-log!
  [msg]
  (with-open [wrtr (io/writer (get env :txlog) :append true)]
    (.write wrtr (str "\n" msg " At: " (t/to-time-zone (t/now) (t/time-zone-for-offset +7))))))

(defn op-log!
  [msg]
  (with-open [wrtr (io/writer (get env :oplog) :append true)]
    (.write wrtr (str "\n" msg " At: " (t/to-time-zone (t/now) (t/time-zone-for-offset +7))))))

(defn sys-log!
  [msg]
  (with-open [wrtr (io/writer (get env :syslog) :append true)]
    (.write wrtr (str "\n" msg " At: " (t/to-time-zone (t/now) (t/time-zone-for-offset +7))))))
