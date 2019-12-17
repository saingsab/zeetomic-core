(ns zeetomic-core.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.util.response :refer [redirect]]
            [ring.middleware.cors :refer [wrap-cors]]
            [schema.core :as s]
            [zeetomic-core.db.users :as users]
            [zeetomic-core.util.conn :as conn]
            [zeetomic-core.account.register :as register]
            [zeetomic-core.account.login :as login]
            [zeetomic-core.account.userinfo :as userinfo]
            [zeetomic-core.account.activation :as activation]
            [zeetomic-core.middleware.auth :as auth]
            [zeetomic-core.operation.getwallet :as getwallet]
            [zeetomic-core.operation.addasset :as addasset]
            [zeetomic-core.operation.pay :as pay]
            [zeetomic-core.loyalty.merchant :as merchants]
            [zeetomic_core.loyalty.receipt :as receipts]))

(s/defschema User-mail
  {:email s/Str
   :password s/Str})

(s/defschema Req-wallet
  {:pin s/Str})

(s/defschema Add-asset
  {:asset-code s/Str
   :asset-ssuer s/Str})

(s/defschema Send-payment
  {:asset-code s/Str
   :destination s/Str
   :amount s/Str
   :memo s/Str})

(s/defschema User-phone
  {:phone s/Str
   :password s/Str})

(s/defschema User-profile
  {:first-name s/Str
   :mid-name s/Str
   :last-name s/Str
   :gender s/Str})

(s/defschema User-coinfirm
  {:phone s/Str
   :verification-code s/Str})

(s/defschema Merchant
  {:merchant-name s/Str})

(s/defschema Merchants
  {:merchant-name s/Str
   :short-name s/Str})

(s/defschema Update-merchant
  {:id s/Str
   :merchant-name s/Str
   :short-name s/Str})

(s/defschema Receipt
  {:receipt-no s/Str
   :amount s/Str
   :location s/Str})

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :options {:ui {:validatorUrl nil}}
     :data {:basePath "/"
            :info {:title "Zeetomic-core"
                   :description "Welcome to the ZEETOMIC API! Zeetomic is a platform APIs are a set of endpoints created to manage integrations with our asset-agnostic global payment and trading platform."
                   :contact {:name "Officail Website"
                             :email "saing@procambodia.com"
                             :url "https://www.zeetomic.com"}}
            :tags [{:name "api", :description "endpoint"}]
            :securityDefinitions {:Authorization_JWT {:type "apiKey"
                                                      :name "Authorization"
                                                      :in "header"}}}}}
   (context "/sec/v1" []
    ;  :no-doc true
     :tags ["SEC API"]
     (GET "/internal" []
       :summary "adds two numbers together"
       :header-params [authorization :- s/Str]
       (ok {:message "Security Partial"})))
   (context "/pub/v1" []
     :tags ["api"]

     (POST "/registerbyemail" []
       :body [user User-mail]
       :summary "Enter correct email and received confirmation"

       (ok (register/accountbyemail (get user :email) (get user :password))))


     (POST "/registerbyphone" []
       :body [user User-phone]
       :summary "Enter correct phone number and received confirmation code"
       (ok (register/accountbyphone (get user :phone) (get user :password))))

     (POST "/loginbyemail" []
       :body [user User-mail]
       :summary "Login with email address get back token"
       (ok (login/loginbyemail (get user :email) (get user :password))))

     (POST "/loginbyphone" []
       :body [user User-phone]
       :summary "Login with phone number get back token"
       (ok (login/loginbyphone (get user :phone) (get user :password))))

     (POST "/userprofile" []
       :header-params [authorization :- s/Str]
       :body [profile User-profile]
       :summary "setup user profile information"
       (ok (userinfo/setup-profile! authorization
                                    (get profile :first-name)
                                    (get profile :mid-name)
                                    (get profile :last-name)
                                    (get profile :gender))))

     (GET "/userprofile" []
       :header-params [authorization :- s/Str]
       :summary "display profile user"
       (ok (userinfo/get-user-profile authorization)))

     (GET "/account-confirmation" []
       :query-params [userid :- s/Str, verification-code :- s/Str]
      ;  (println (activation/activate-user userid verification-code))
       (if (= (activation/activate-user userid verification-code) true)
         (redirect "https://www.zeetomic.com/successfullyverified")
         (redirect "https://www.zeetomic.com/failedverification")))

     (POST "/account-confirmation" []
       :body [user-confirm User-coinfirm]
       :summary "Confirm user account from phone"
       (if (= (activation/activate-user
               (get (activation/user-by-phone (get user-confirm :phone)) :id)
               (get user-confirm :verification-code)) true)
         (ok {:message "User successfully activated"})
         (ok {:error {:message "User failed activation"}})))

     (POST "/getwallet" []
       :body [req-wallet Req-wallet]
       :header-params [authorization :- s/Str]
       (ok (getwallet/gen-wallet authorization (get req-wallet :pin))))

     (POST "/addasset" []
       :body [add-asset Add-asset]
       :header-params [authorization :- s/Str]
       (ok (addasset/accept-asset? authorization
                                   (get add-asset :asset-code)
                                   (get add-asset :asset-ssuer))))

     (POST "/sendpayment" []
       :header-params [authorization :- s/Str]
       :body [send-payment Send-payment]
       (ok (pay/pay! authorization
                     (get send-payment :asset-code)
                     (get send-payment :destination)
                     (get send-payment :amount)
                     (get send-payment :memo))))

     (POST "/add-merchant" []
       :header-params [authorization :- s/Str]
       :body [merchant Merchants]
       (ok (merchants/add-merchant! authorization
                                    (get merchant :merchant-name)
                                    (get merchant :short-name))))

     (POST "/update-merchant" []
       :header-params [authorization :- s/Str]
       :body [update-merchant Update-merchant]
       (ok (merchants/update-merchant? authorization
                                       (get update-merchant :id)
                                       (get update-merchant :merchant-name)
                                       (get update-merchant :short-name))))

     (POST "/get-merchant-by-name" []
       :header-params [authorization :- s/Str]
       :summary "Get Merchant information by name"
       :body [merchant Merchant]
       (merchants/get-merchant-by-name authorization
                                       (get merchant :merchant-name)))

     (GET "/get-merchant" []
       :header-params [authorization :- s/Str]
       :summary "Get list merchant by creator"
       (ok (merchants/get-merchant-by-owner authorization)))

     (GET "/get-all-merchants" []
       :header-params [authorization :- s/Str]
       :summary "List all merchants"
       (ok (merchants/get-all-merchants authorization)))

     (POST "/addreceipt" []
       :header-params [authorization :- s/Str]
       :body [receipt Receipt]
       (ok (receipts/add-receipt! authorization
                                  (get receipt :receipt-no)
                                  (get receipt :amount)
                                  (get receipt :location))))

     (GET "/get-receipt" []
       :header-params [authorization :- s/Str]
       :summary "List all receipt activity"
       (ok (receipts/get-receipt authorization)))



; next
     )))

(def handler
  (wrap-cors app :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :post]))