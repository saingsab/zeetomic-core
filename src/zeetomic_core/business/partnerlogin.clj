(ns zeetomic-core.business.partnerlogin
  (:require [zeetomic-core.util.conn :as conn]
            [zeetomic-core.middleware.auth :as auth]
            [clj-http.client :as client]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.db.status :as status]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [zeetomic-core.util.validate :as validate]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [ring.util.http-response :refer :all]
            [zeetomic-core.util.writelog :as writelog]
            [aero.core :refer (read-config)]))

(def env (read-config ".config.edn"))

(defn tokens
    ; 1day to be expired
  [info]
  (jwt/sign {:_id info :exp (time/plus (time/now) (time/seconds 86400))} (get (read-config ".config.edn") :jwtsec)))

(def status-id
  (get (status/get-status-by-name conn/db {:STATUS_NAME "inactive"}) :id))

(defn email-not-exist?
  [email]
  (nil? (users/get-users-by-mail conn/db {:EMAIL email})))

(defn is-no-active-mail?
  [email]
  (= status-id (get (users/get-users-by-mail conn/db {:EMAIL email}) :id)))

(defn id-by-email
  [email]
  (get (users/get-users-by-mail conn/db {:EMAIL email}) :id))

(defn is-partner?
  [email]
  (= true (get (users/get-users-by-mail conn/db {:EMAIL email}) :is_partner)))

(defn partner-login
  [email password]
  (if (= (validate/email? email) true)
    (if (= (email-not-exist? email) true)
      (ok {:message "Your email address does not exist!"})
      (if (= true (is-partner? email))
        (if (= true (hashers/check password (get (users/get-users-by-mail conn/db {:EMAIL email}) :password)))
          (ok {:token (tokens (id-by-email email))})
          (ok {:error {:message "Login failed the username or password is incorrect"}}))
        (ok {:message "Your email address does not associated with partner program"})))
    (ok {:message "Your email doesn't seem right!"})))