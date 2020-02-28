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
            [zeetomic-core.waves.wallet :as waves-wallet]
            [zeetomic-core.waves.portforlio :as waves-portforlio]
            [zeetomic-core.waves.txhistory :as waves-txhistory]
            [zeetomic-core.waves.transfer :as waves-transfer]
            [zeetomic-core.operation.addasset :as addasset]
            [zeetomic-core.operation.pay :as pay]
            [zeetomic-core.loyalty.merchant :as merchants]
            [zeetomic-core.loyalty.branches :as branches]
            [zeetomic_core.loyalty.receipt :as receipts]
            [zeetomic-core.sto.whitelist :as whitelist]))

(s/defschema User-mail
  {:email s/Str
   :password s/Str})

(s/defschema Req-wallet
  {:pin s/Str})

(s/defschema Add-asset
  {:asset_code s/Str
   :asset_issuer s/Str})

(s/defschema Send-payment
  {:pin s/Str
   :asset_code s/Str
   :destination s/Str
   :amount s/Str
   :memo s/Str})

(s/defschema User-phone
  {:phone s/Str
   :password s/Str})

(s/defschema User-profile
  {:first_name s/Str
   :mid_name s/Str
   :last_name s/Str
   :gender s/Str})

(s/defschema User-coinfirm
  {:phone s/Str
   :verification_code s/Str})

(s/defschema Merchant
  {:merchant_name s/Str})

(s/defschema Merchants
  {:merchant_name s/Str
   :short_name s/Str})

(s/defschema Update-merchant
  {:id s/Str
   :merchant_name s/Str
   :short_name s/Str})

(s/defschema Branches
  {:merchant_id s/Str
   :branches_name s/Str
   :address s/Str
   :reward_rates s/Str
   :asset_code s/Str
   :minimum_spend s/Str
   :approval_code s/Str})

(s/defschema Update-Branches
  {:branches_name s/Str
   :address s/Str
   :reward_rates s/Str
   :asset_code s/Str
   :minimum_spend s/Str
   :approval_code s/Str
   :is-active s/Bool})


(s/defschema Receipt
  {:receipt_no s/Str
   :amount s/Str
   :location s/Str
   :image_uri s/Str
   :approval_code s/Str})

(s/defschema Phone
  {:phone s/Str})

(s/defschema Reset-password
  {:temp_code s/Str
   :phone s/Str
   :password s/Str})

(s/defschema Change-pin
  {:current_pin s/Str
   :new_pin s/Str})

(s/defschema Change-password
  {:current_password s/Str
   :new_password s/Str})

; Waves Schema
(s/defschema Waves-payment
  {:pin s/Str
  ;  :asset_code s/Str
   :destination s/Str
   :amount s/Str})
  ;  :memo s/Str})

(s/defschema Kpi-whitelist
  {:trustoracc s/Str
   :assetcode s/Str
   :authcode s/Str})

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
   (context "/kpi/v1" []
    ;  :no-doc true
     :tags ["KPI"]
     (POST "/whitelist" []
       :header-params [authorization :- s/Str]
       :body [kpi-whitelist Kpi-whitelist]
       :summary "whitlest wallet to that can hold KPI token"

       (whitelist/whitelist!
        authorization
        (get kpi-whitelist :trustoracc)
        (get kpi-whitelist :assetcode)
        (get kpi-whitelist :authcode))))

   (context "/ke/v1" []
    ;  :no-doc true
     :tags ["KE TOKEN API"]
     (POST "/wallet" []
       :body [req-wallet Req-wallet]
       :header-params [authorization :- s/Str]
       :summary "Provide PIN and get wallet"
       (waves-wallet/gen-wallet authorization
                                (get req-wallet :pin)))
     (GET "/portforlio" []
       :header-params [authorization :- s/Str]
       :summary "display portfolio on user base"
       (waves-portforlio/get-portforlio authorization))

     (POST "/sendpayment" []
       :header-params [authorization :- s/Str]
       :body [send-payment Waves-payment]
       :summary "Sending payment to other wallet"
       (waves-transfer/send-payment authorization
                                    (get send-payment :amount)
                                    (get send-payment :destination)))

     (GET "/trx-history" []
       :header-params [authorization :- s/Str]
       :summary "display detail transaction history"
       (waves-txhistory/get-txhistory authorization))
    ; end of KE endpoint                             
     )

   (context "/pub/v1" []
     :tags ["api"]

     (POST "/registerbyemail" []
       :body [user User-mail]
       :summary "Enter correct email and received confirmation"
       (register/accountbyemail (get user :email) (get user :password)))

     (POST "/registerbyphone" []
       :body [user User-phone]
       :summary "Enter correct phone number and received confirmation code"
       (register/accountbyphone (get user :phone) (get user :password)))

     (POST "/loginbyemail" []
       :body [user User-mail]
       :summary "Login with email address get back token"
       (login/loginbyemail (get user :email) (get user :password)))

     (POST "/loginbyphone" []
       :body [user User-phone]
       :summary "Login with phone number get back token"
       (login/loginbyphone (get user :phone) (get user :password)))

     (POST "/userprofile" []
       :header-params [authorization :- s/Str]
       :body [profile User-profile]
       :summary "setup user profile information"
       (userinfo/setup-profile! authorization
                                (get profile :first_name)
                                (get profile :mid_name)
                                (get profile :last_name)
                                (get profile :gender)))

     (GET "/userprofile" []
       :header-params [authorization :- s/Str]
       :summary "display profile user"
       (userinfo/get-user-profile authorization))

     (GET "/account-confirmation" []
       :query-params [userid :- s/Str, verification-code :- s/Str]
       :summary "Only show for email!"
       (if (= (activation/activate-user userid verification-code) true)
         (redirect "https://www.zeetomic.com/successfullyverified")
         (redirect "https://www.zeetomic.com/failedverification")))

     (GET "/portforlio" []
       :header-params [authorization :- s/Str]
       :summary "display portfolio on user base"
       (pay/portforlio authorization))

     (GET "/trx-history" []
       :header-params [authorization :- s/Str]
       :summary "display detail transaction history"
       (pay/trx-hostory authorization))

     (POST "/account-confirmation" []
       :body [user-confirm User-coinfirm]
       :summary "Confirm user account from phone"
       (if (= (activation/activate-user-by-phone
               (get user-confirm :phone)
               (get user-confirm :verification_code)) true)
         (ok {:message "User successfully activated"})
         (ok {:error {:message "User failed activation"}})))

     (POST "/getwallet" []
       :body [req-wallet Req-wallet]
       :header-params [authorization :- s/Str]
       :summary "Provide PIN and get wallet"
       (getwallet/gen-wallet authorization (get req-wallet :pin)))

     (POST "/addasset" []
       :body [add-asset Add-asset]
       :header-params [authorization :- s/Str]
       :summary "Add existing Asset into portforlio"
       (addasset/accept-asset? authorization
                               (get add-asset :asset_code)
                               (get add-asset :asset_issuer)))

     (POST "/sendpayment" []
       :header-params [authorization :- s/Str]
       :body [send-payment Send-payment]
       :summary "Sending payment to other wallet"
       (pay/pay! authorization
                 (get send-payment :pin)
                 (get send-payment :asset_code)
                 (get send-payment :destination)
                 (get send-payment :amount)
                 (get send-payment :memo)))

     (POST "/add-merchant" []
       :header-params [authorization :- s/Str]
       :body [merchant Merchants]
       :summary "[Partners only]"
       (merchants/add-merchant! authorization
                                (get merchant :merchant_name)
                                (get merchant :short_name)))

     (POST "/update-merchant" []
       :header-params [authorization :- s/Str]
       :body [update-merchant Update-merchant]
       :summary "Enter correct merchant infomation"
       (merchants/update-merchant? authorization
                                   (get update-merchant :id)
                                   (get update-merchant :merchant_name)
                                   (get update-merchant :short_name)))

     (POST "/get-merchant-by-name" []
       :header-params [authorization :- s/Str]
       :summary "Get Merchant information by name"
       :body [merchant Merchant]
       (merchants/get-merchant-by-name authorization
                                       (get merchant :merchant_name)))

     (GET "/get-merchant" []
       :header-params [authorization :- s/Str]
       :summary "Get list merchant by creator"
       (merchants/get-merchant-by-owner authorization))

     (GET "/get-all-merchants" []
       :header-params [authorization :- s/Str]
       :summary "List all merchants"
       (merchants/get-all-merchants authorization))

     (POST "/add-branches" []
       :header-params [authorization :- s/Str]
       :body [branches Branches]
       :summary "Enter correct branches and other important param for reward system!"
       (branches/add-branches! authorization
                               (get branches :merchant_id)
                               (get branches :branches_name)
                               (get branches :address)
                               (get branches :reward_rates)
                               (get branches :asset_code)
                               (get branches :minimum_spend)
                               (get branches :approval_code)))

     (POST "/update-branches" []
       :header-params [authorization :- s/Str]
       :body [update-branches Update-Branches]
       :summary "Enter correct branches confirmation"
       (branches/update-branches? authorization
                                  (get update-branches :branches_name)
                                  (get update-branches :address)
                                  (get update-branches :reward_rates)
                                  (get update-branches :asset_code)
                                  (get update-branches :minimum_spend)
                                  (get update-branches :approval_code)
                                  (get update-branches :is-active)))
     (GET "/get-all-branches" []
       :header-params [authorization :- s/Str]
       :summary "Listing all branches"
       (branches/list-all-branches! authorization))

     (POST "/addreceipt" []
       :header-params [authorization :- s/Str]
       :body [receipt Receipt]
       :summary "Upload receipt and Enter correct aprroval or authorized code from counter"
       (receipts/add-receipt! authorization
                              (get receipt :receipt_no)
                              (get receipt :amount)
                              (get receipt :location)
                              (get receipt :image_uri)
                              (get receipt :approval_code)))

     (GET "/get-receipt" []
       :header-params [authorization :- s/Str]
       :summary "List all receipt activity"
       (receipts/get-receipt authorization))

     (POST "/forget-password" []
       :summary "Input User phone number to received reseting code"
       :body [phone Phone]
       (login/forget-password (get phone :phone)))

     (POST "/reset-password" []
       :summary "Enter a valid reseting code and new password"
       :body [reset-password Reset-password]
       (login/reset-password! (get reset-password :temp-code)
                              (get reset-password :phone)
                              (get reset-password :password)))

     (POST "/change-pin" []
       :header-params [authorization :- s/Str]
       :summary "Enter current PIN and new PIN to change!"
       :body [change-pin Change-pin]
       (login/change-pin! authorization
                          (get change-pin :current_pin)
                          (get change-pin :new_pin)))

     (POST "/change-password" []
       :header-params [authorization :- s/Str]
       :summary "Enter current Password and new Password to change!"
       :body [change-password Change-password]
       (login/change-password! authorization
                               (get change-password :current_password)
                               (get change-password :new_password)))
; next
     )))

(def handler
  (wrap-cors app :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :post]))