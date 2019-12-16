(ns zeetomic-core.middleware.auth
  (:require [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            [aero.core :refer (read-config)]))

(defn token?
  [token]
  (if (= true (.contains token "Bearer"))
    (try
      (let [payload (jwt/unsign (nth (str/split token #" ") 1) (get (read-config ".config.edn") :jwtsec))] payload)
      (catch Exception ex
        ""))
    {:error {:message "Unauthorized access denied"}}))

(defn authorized?
  [token]
  (if (= (token? token) "")
    {:error {:message "Unauthorized access denied"}}
    (try
      (if (= (get (token? token) :_id) (get (users/get-users-token conn/db {:ID (get (token? token) :_id)}) :id))
        true
        false)
      (catch Exception ex
        {:error {:message "Internal server error"}}))))