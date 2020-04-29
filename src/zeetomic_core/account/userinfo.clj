(ns zeetomic-core.account.userinfo
  (:require [clojure.tools.logging :as log]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as stu]
            [zeetomic-core.db.documents :as documents]
            [zeetomic-core.db.documenttype :as documenttype]
            [zeetomic-core.middleware.auth :as auth]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]))

(defn uuid [] (str (java.util.UUID/randomUUID)))
(def docs-id (atom (uuid)))

(def Status
  (get (stu/get-status-by-name conn/db {:STATUS_NAME "active"}) :id))

(defn phone-not-exist?
  [phone]
  (nil? (users/get-users-by-phone conn/db {:PHONENUMBER phone})))

(defn email-not-exist?
  [email]
  (nil? (users/get-users-by-mail conn/db {:EMAIL email})))

(defn get-documenttype
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (documenttype/get-all-documenttype conn/db))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : FN GET DOCUMENTTYPE " (.getMessage ex)))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn setup-profile!
  [token first-name mid-name last-name gender]
  (if (= (auth/authorized? token) true)
  ; letdo
    (try
      (users/setup-user-profile conn/db {:ID (get (auth/token? token) :_id)
                                         :FIRST_NAME first-name
                                         :MID_NAME mid-name
                                         :LAST_NAME last-name
                                         :GENDER gender
                                         :STATUS_ID Status})
      (ok {:message "Your profile have been saved successfully"})
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        {:error {:message "Something went wrong on our end"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn get-user-profile
  [token]
  (if (= (auth/authorized? token) true)
    (try
      (ok (users/get-users-by-id conn/db {:ID (get (auth/token? token) :_id)}))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

(defn set-kyc!
  [token document_no documenttype_id document_uri face_uri issue_date expire_date]
  (if (= (auth/authorized? token) true)
    (try
      (println "seting KYC...")
      (documents/set-documents conn/db {:ID @docs-id :DOCUMENTS_NO document_no :DOCUMENTTYPE_ID documenttype_id :DOCUMENT_URI document_uri :FACE_URI face_uri :ISSUE_DATE issue_date :EXPIRE_DATE expire_date :CREATED_BY (get (auth/token? token) :_id)})
      (ok {:message "Documents have been submitted "})
      (reset! docs-id (uuid))
      (catch Exception ex
        (writelog/op-log! (str "ERROR : FN SET-KYC" (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))