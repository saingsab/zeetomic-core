(ns zeetomic-core.migration.dbmigration
  (:require [zeetomic-core.util.conn :as conn]))

(def status
  (str (get (mc/find-one-as-map  conn/DB "STATUS" {:STATUS_NAME "inactive"}) :_id)))

(def documenttype
  (str (get (mc/find-one-as-map  conn/DB "STATUS" {:STATUS_NAME "inactive"}) :_id)))

(defn migrate-status []
  (println "... Start Input STATUS...." (str (java.time.LocalDateTime/now)))
  (try
    (mc/insert-batch conn/DB "STATUS" [{:STATUS_NAME "inactive"}
                                       {:STATUS_NAME "active"}
                                       {:STATUS_NAME "verifying"}
                                       {:STATUS_NAME "verified"}
                                       {:STATUS_NAME "disabled"}])
    (catch Exception ex))

  (println "Finished! STATUS " (str (java.time.LocalDateTime/now))))

(defn migrate-documenttype []
  (println "... Start Input DOCUMENTTYPE...." (str (java.time.LocalDateTime/now)))
  (try
    (mc/insert-batch conn/DB "DOCUMENTTYPE" [{:DOCUMENT_NAME "National ID"}
                                             {:DOCUMENT_NAME "Passport"}
                                             {:DOCUMENT_NAME "Driver License"}])
    (catch Exception ex
      (println ex)))
  (println "Finished! DOCUMENTTYPE " (str (java.time.LocalDateTime/now))))

(defn migrating
  []
  (if (= "" status)
    (migrate-status)
    (println "Data already exist!"))
  (if (= "" documenttype)
    (migrate-documenttype)
    (println "Data already exist!")))