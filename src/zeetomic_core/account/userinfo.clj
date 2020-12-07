(ns zeetomic-core.account.userinfo
  (:require [clojure.tools.logging :as log]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as stu]
            [zeetomic-core.db.documents :as documents]
            [zeetomic-core.db.documenttype :as documenttype]
            [zeetomic-core.middleware.auth :as auth]
            [clj-http.client :as client]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.genpin :as genpin]
            [aero.core :refer (read-config)]
            [zeetomic-core.util.writelog :as writelog]))

(def env (read-config ".config.edn"))
(defn uuid [] (str (java.util.UUID/randomUUID)))
(def docs-id (atom (uuid)))

(def pin-code (atom (genpin/getpin)))

(def Status
  (get (stu/get-status-by-name conn/db {:STATUS_NAME "active"}) :id))

(def Status-verifying
  (get (stu/get-status-by-name conn/db {:STATUS_NAME "verifying"}) :id))

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
  [token first-name mid-name last-name gender image-uri address]
  (if (= (auth/authorized? token) true)
  ; letdo
    (try
      (users/setup-user-profile conn/db {:ID (get (auth/token? token) :_id)
                                         :FIRST_NAME first-name
                                         :MID_NAME mid-name
                                         :LAST_NAME last-name
                                         :GENDER gender
                                         :PROFILE_IMG image-uri
                                         :ADDRESS address
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
  [token nationality occupation address document_no documenttype_id document_uri face_uri issue_date expire_date]
  (if (= (auth/authorized? token) true)
    (try
      (println "seting KYC...")
      (documents/set-documents conn/db {:ID @docs-id
                                        :DOCUMENTS_NO document_no
                                        :DOCUMENTTYPE_ID documenttype_id
                                        :DOCUMENT_URI document_uri
                                        :FACE_URI face_uri
                                        :ISSUE_DATE issue_date
                                        :EXPIRE_DATE expire_date
                                        :CREATED_BY (get (auth/token? token) :_id)})
      ;; Update User Status to Verifying
      (users/update-status conn/db {:ID (get (auth/token? token) :_id)
                                    :NATIONALITY nationality
                                    :OCCUPATION occupation
                                    :ADDRESS address
                                    :STATUS_ID  (get (stu/get-status-by-name conn/db {:STATUS_NAME "verifying"}) :id)})
      ; Update Document ID Into User Table
      (users/update-doc-id conn/db {:ID (get (auth/token? token) :_id)
                                    :DOCUMENTS_ID  @docs-id})
      ; (println "Reseting UUID...")
      (reset! docs-id (uuid))
      ; (println "Finish Jobs sending a response...")
      (ok {:message "Documents have been submitted successfully"})
      (catch Exception ex
        (writelog/op-log! (str "ERROR : FN SET-KYC" (.getMessage ex)))
        (ok {:error {:message "Something went wrong on our end"}})))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))

    ; Verify valid phone first before add to db
  (defn add-phone-number
    [token phone]
    (if (= (auth/authorized? token) true)
      (try
        (users/set-phonenumber-by-id conn/db {:ID (get (auth/token? token) :_id) :PHONENUMBER phone :TEMP_TOKEN @pin-code})
        (client/post (str (get env :smsendpoint)) {:form-params {:smscontent (str "Your SELENDRA verification code is:" @pin-code) :phonenumber phone} :content-type :json}) 
        (reset! pin-code (genpin/getpin))
        (ok {:message (str "We've sent you an SMS with the code to " phone)})
        (catch Exception ex
          (writelog/op-log! (str "ERROR : FN add-phone-number" (.getMessage ex)))
          (ok {:error {:message "Something went wrong on our end"}})))
      (unauthorized {:error {:message "Unauthorized operation not permitted"}})))