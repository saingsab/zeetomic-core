(ns zeetomic-core.account.login
  (:require [zeetomic-core.util.conn :as conn]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as status]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]
            [zeetomic-core.util.validate :as validate]
            [clj-time.core :as time]
            [aero.core :refer (read-config)]))

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
      {:message "Your email address does not exist!"}
      (if (= (is-no-active-mail? email) false)
        (if (= true (hashers/check password (get (users/get-users-by-mail conn/db {:EMAIL email}) :password)))
          {:token (tokens (id-by-email email))}
          {:error {:message "Login failed the username or password is incorrect"}})
        {:error {:message "Sorry, your user login and password are not active"}}))
    {:message "Your email doesn't seem right!"}))

(defn loginbyphone
  [phone password]
  (if (= (validate/phone? phone) true)
    (if (= (phone-not-exist? phone) true)
      {:message "Your phone number does not exist!"}

      (if (= (is-no-active-phone? phone) false)
        (if (= true (hashers/check password (get (users/get-users-by-phone conn/db {:PHONENUMBER phone}) :password)))
          {:token (tokens (id-by-phone phone))}
          {:error {:message "Login failed the username or password is incorrect"}})
        {:error {:message "Sorry, your user login and password are not active"}}))
    {:message "Your phone number doesn't seem right!"}))
