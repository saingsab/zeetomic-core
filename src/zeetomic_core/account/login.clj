(ns zeetomic-core.account.login
  (:require [zeetomic-core.util.conn :as conn]
            [zeetomic-core.middleware.auth :as auth]
            [clj-http.client :as client]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as status]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]
            [zeetomic-core.util.genpin :as genpin]
            [zeetomic-core.util.validate :as validate]
            [zeetomic-core.util.ed :as ed]
            [clj-time.core :as time]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]
            [aero.core :refer (read-config)]))

(def env (read-config ".config.edn"))

(def pin-code (atom (genpin/getpin)))

(def status-id
  (get (status/get-status-by-name conn/db {:STATUS_NAME "inactive"}) :id))

(defn phone-not-exist?
  [phone]
  (nil? (users/get-users-by-phone conn/db {:PHONENUMBER phone})))

(defn email-not-exist?
  [email]
  (nil? (users/get-users-by-mail conn/db {:EMAIL email})))

(defn id-by-email
  [email]
  (get (users/get-users-by-mail conn/db {:EMAIL email}) :id))

(defn id-by-phone
  [phone]
  (get (users/get-users-by-phone conn/db {:PHONENUMBER phone}) :id))

(defn tokens
    ; 1day to be expired
  [info]
  (jwt/sign {:_id info :exp (time/plus (time/now) (time/seconds 86400))} (get (read-config ".config.edn") :jwtsec)))

(defn is-no-active-mail?
  [email]
  (= status-id (get (users/get-users-by-mail conn/db {:EMAIL email}) :id)))

(defn is-no-active-phone?
  [phone]
  (= status-id (get (users/get-users-by-phone conn/db {:PHONENUMBER phone}) :id)))

(defn loginbyemail
  [email password]
  (if (= (validate/email? email) true)
    (if (= (email-not-exist? email) true)
      (ok {:message "Your email address does not exist!"})
      (if (= (is-no-active-mail? email) false)
        (if (= true (hashers/check password (get (users/get-users-by-mail conn/db {:EMAIL email}) :password)))
          (ok {:token (tokens (id-by-email email))})
          (ok {:error {:message "Login failed the username or password is incorrect"}}))
        (ok {:error {:message "Sorry, your user login and password are not active"}})))
    (ok {:message "Your email doesn't seem right!"})))

(defn loginbyphone
  [phone password]
  (if (= (validate/phone? phone) true)
    (if (= (phone-not-exist? phone) true)
      (ok {:message "Your phone number does not exist!"})
      (if (= (is-no-active-phone? phone) false)
        (if (= true (hashers/check password (get (users/get-users-by-phone conn/db {:PHONENUMBER phone}) :password)))
          (try
            (ok {:token (tokens (id-by-phone phone))})
            (catch Exception ex
              (writelog/op-log! (str "ERROR : " (.getMessage ex)))
              (ok {:error {:message "Something went wrong on our end"}})))
          (ok {:error {:message "Login failed the username or password is incorrect"}}))
        (ok {:error {:message "Sorry, your user login and password are not active"}})))
    (ok {:message "Your phone number doesn't seem right!"})))

(defn forget-password
  [phone]
  (if (= (phone-not-exist? phone) true)
    (ok {:message "Your phone number does not exist!"})
    (try
    ; Create Temp code
      (users/update-temp conn/db {:PHONENUMBER phone :TEMP_TOKEN @pin-code})
   ; Sending temp code code.
      (client/post (str (get env :smsendpoint)) {:form-params {:smscontent (str "Your ZEETOMIC reset code is:" @pin-code) :phonenumber phone} :content-type :json})
      (reset! pin-code (genpin/getpin))
      (ok {:message "We have sent you reset code please check your SMS!"})
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))))))

(defn reset-password!
  [temp-code phone password]
  (if (= temp-code (get (users/get-users-by-phone conn/db {:PHONENUMBER phone}) :temp_token))
    (try
    ; match user and start reset new password
      (users/reset-password conn/db {:PHONENUMBER phone :PASSWORD (hashers/derive password)})
      (ok {:message "Your password successfully updated!"})
      (catch Exception ex
        (writelog/op-log! (str "ERROR : " (.getMessage ex)))))
    (ok {:error {:message "Opps! your reset code was not correct!"}})))

(defn change-pin!
  [token current-pin new-pin]
  (if (= (auth/authorized? token) true)
    (if (hashers/check current-pin (get (users/get-pin-by-id conn/db {:ID (get (auth/token? token) :_id)}) :pin))
      (try
        (users/set-pin conn/db {:ID (get (auth/token? token) :_id) :PIN (hashers/derive new-pin)})
        (ok {:message "Your PIN successfully changed!"})
        (catch Exception ex
          (writelog/op-log! (str "ERROR : " (.getMessage ex)))))
      (ok {:error {:message "Opps! your current PIN was not correct!"}}))
    (unauthorized {:error {:message "Unauthorized operation not permitted"}})))