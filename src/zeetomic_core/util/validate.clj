(ns zeetomic-core.util.validate
  (:require [clojure.string :as str]))

(def non-blank? (complement str/blank?))

(defn max-length? [length text]
  (<= (count text) length))

(defn non-blank-with-max-length? [length text]
  (and (non-blank? text) (max-length? length text)))

(defn min-length? [length text]
  (>= (count text) length))

(defn length-in-range? [min-length max-length text]
  (and (min-length? min-length text) (max-length? max-length text)))

(def email-regex
  #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")

(def phone-regex
  #"^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\s\./0-9]*$")

(defn email? [email]
  (boolean (and (string? email) (re-matches email-regex email))))

(defn phone? [phone]
  (boolean (re-matches phone-regex phone)))