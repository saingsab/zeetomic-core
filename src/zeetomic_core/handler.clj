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
            [zeetomic-core.business.partnerlogin :as partnerlogin]
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
            [zeetomic-core.sto.whitelist :as whitelist]
            [zeetomic-core.loyalty.genreward :as genreward]
            [zeetomic-core.account.walletlookup :as walletlookup]
            ; Selendra market place
            [zeetomic_core.sdm.sdm-products :as sdm-products]
            [zeetomic_core.sdm.sdm-orders :as sdm-orders]
            [zeetomic_core.sdm.sdm-order-status :as sdm-order-status]
            [zeetomic_core.sdm.sdm-payment-options :as sdm-payment-options]
            [zeetomic_core.sdm.sdm-product-categories :as sdm-product-categories]
            [zeetomic_core.sdm.sdm-products-images :as sdm-products-images]
            [zeetomic_core.sdm.sdm-shipping :as sdm-shipping]
            [zeetomic_core.sdm.sdm-weight :as sdm-weight]))

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

(s/defschema Branches-granted
  {:branches_name s/Str
   :email s/Str})

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
   :approval_code s/Str
   :logo_uri s/Str})

(s/defschema Update-Branches
  {:branches_name s/Str
   :address s/Str
   :reward_rates s/Str
   :asset_code s/Str
   :minimum_spend s/Str
   :approval_code s/Str
   :logo_uri s/Str
   :is_active s/Bool})


(s/defschema Receipt
  {:receipt_no s/Str
   :amount s/Str
   :location s/Str
   :image_uri s/Str
   :approval_code s/Str})

(s/defschema Reports-from-to-date
  {:from_date s/Str
   :to_date s/Str})

(s/defschema Reports-from-to-date-by-location
  {:from_date s/Str
   :to_date s/Str
   :location s/Str})

(s/defschema Reports-by-location
  {:location s/Str})

(s/defschema Genqr
  {:location s/Str
   :approval_code s/Str
   :receipt_no s/Str
   :amount s/Str})

(s/defschema Getreward
  {:hashs s/Str})

(s/defschema Phone
  {:phone s/Str})

(s/defschema Email
  {:email s/Str})

(s/defschema Reset-password
  {:temp_code s/Str
   :phone s/Str
   :password s/Str})

(s/defschema Reset-password-by-email
  {:temp_code s/Str
   :email s/Str
   :password s/Str})

(s/defschema Change-pin
  {:current_pin s/Str
   :new_pin s/Str})

(s/defschema Change-password
  {:current_password s/Str
   :new_password s/Str})

(s/defschema Set-kyc
  {:nationality s/Str
   :occupation s/Str
   :address s/Str
   :document_no s/Str
   :documenttype_id s/Str
   :document_uri s/Str
   :face_uri s/Str
   :issue_date s/Str
   :expire_date s/Str})

(s/defschema Wallet-lookup
  {:phone s/Str})

; APIKEY
(s/defschema Apikeys
  {:apikey s/Str
  :apisec s/Str})

  (s/defschema Paybyapi
    {:id s/Str
     :apikey s/Str 
     :apisec s/Str 
     :destination s/Str 
     :asset_code s/Str 
     :amount s/Str 
     :memo s/Str})


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

(s/defschema OAuth-token 
  {:token s/Str})

; Selendra Market Place Schema
(s/defschema Sdm-add-product
  { :name s/Str
    :price s/Str
    :shipping s/Str
    :weight s/Str
    :description s/Str
    :thumbnail s/Str
    :category-id s/Str
    :payment-id s/Str})

(s/defschema Sdm-make-order 
 { :product-id s/Str 
   :qty s/Str
   :shipping-address s/Str})

(s/defschema Sdm-order-status 
  {:order-id s/Str})

(s/defschema Sdm-products-images
  {:url s/Str
  :product-id s/Str})

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :options {:ui {:validatorUrl nil}}
     :data {:basePath "/"
            :info {:title "Selendra-biz"
                   :description "Welcome to the SELENDRA API! Selendra is a platform APIs are a set of endpoints created to manage integrations with our asset-agnostic global payment and trading platform."
                   :contact {:name "Officail Website"
                             :email "saing@selendra.org"
                             :url "https://www.selendra.com"}}
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
       :summary "Whitelist wallet for KPI token holder"

       (whitelist/whitelist!
        authorization
        (get kpi-whitelist :trustoracc)
        (get kpi-whitelist :assetcode)
        (get kpi-whitelist :authcode))))
  ; OAuth ID token
  (context "/oauth/v1" []
  ;  Uncomment no-doc to hide document
  ; :no-doc true
    :tags ["OAuth ID token"]
    (POST "/login-from-google" []
      :body [oauth-token OAuth-token]
      :summary "Provide OAuth token return JWT token"
      (login/login-from-google (get oauth-token :token)))
      
    (POST "/login-from-facebook" []
      :body [oauth-token OAuth-token]
      :summary "Provide OAuth token return JWT token"
      (login/login-from-facebook  (get oauth-token :token))))
      
  ; EXTERNAL API
  (context "/apis/v1" []
    :tags ["APIS"]
    (GET "/request-api-key" []
      :header-params [authorization :- s/Str]
      :summary "Provide valide Token to return api keys"
      (merchants/get-apikey authorization))

    (POST "/get-wallet" []
      :body [apikeys Apikeys]
      :summary "Provide api key to get wallet"
    (getwallet/gen-wallet-by-api (get apikeys :apikey) 
                                 (get apikeys :apisec)))
    
    (POST "/payment" []
      :body [paybyapi Paybyapi]
      :summary "Pay from api to wallet"
      (pay/pay-by-api (get paybyapi :id) 
                   (get paybyapi :apikey) 
                   (get paybyapi :apisec) 
                   (get paybyapi :destination) 
                   (get paybyapi :asset_code) 
                   (get paybyapi :amount) 
                   (get paybyapi :memo)))
  ; next endpoine for external APIS
  )
  ; Selendra Market Endpoint
  (context "/sdm/v1" []
    ;  :no-doc true
     :tags ["Selendra Market API"]
    ;  (POST "/wallet" []
    ;    :body [req-wallet Req-wallet]
    ;    :header-params [authorization :- s/Str]
    ;    :summary "Provide PIN and get wallet"
    ;    (waves-wallet/gen-wallet authorization
    ;                             (get req-wallet :pin)))
     (GET "/listing" []
       :header-params [authorization :- s/Str]
       :summary "Products listing"
       (sdm-products/get-all-products authorization))

     (GET "/listing-by-owner" []
       :header-params [authorization :- s/Str]
       :summary "Products listing by owner"
       (sdm-products/get-products-by-owner authorization))
     (POST "/add-product" []
       :body [sdm-add-product Sdm-add-product]
       :header-params [authorization :- s/Str]
       :summary "Add product to listing"
       (sdm-products/add-products authorization
                                  (get sdm-add-product :name)
                                  (get sdm-add-product :price)
                                  (get sdm-add-product :shipping)
                                  (get sdm-add-product :weight)
                                  (get sdm-add-product :description)
                                  (get sdm-add-product :thumbnail)
                                  (get sdm-add-product :category-id)
                                  (get sdm-add-product :payment-id)))
                       
     (GET "/list-order" []
       :header-params [authorization :- s/Str]
       :summary "List product order"
       (sdm-orders/list-order authorization))
      
    (POST "/make-order" []
       :body [sdm-make-order Sdm-make-order]
       :header-params [authorization :- s/Str]
       :summary "Making order product"
       (sdm-orders/make-orders authorization
                                  (get sdm-make-order :product-id) 
                                  (get sdm-make-order :qty)
                                  (get sdm-make-order :shipping-address)))
      
    (POST "/mark-order-payment" []
       :body [sdm-order-status Sdm-order-status]
       :header-params [authorization :- s/Str]
       :summary "Mark payment on order"
       (sdm-orders/update-order-success-pay authorization
                                  (get sdm-order-status :order-id)))    
                                  
    (POST "/mark-order-shipment" []
       :body [sdm-order-status Sdm-order-status]
       :header-params [authorization :- s/Str]
       :summary "Mark shipment on order"
       (sdm-orders/update-order-shipment authorization
                                  (get sdm-order-status :order-id)))

    (POST "/mark-order-completed" []
      :body [sdm-order-status Sdm-order-status]
      :header-params [authorization :- s/Str]
      :summary "Mark completed on order"
      (sdm-orders/update-order-shipment authorization
                                (get sdm-order-status :order-id)))

    (GET "/order-status" []
      :header-params [authorization :- s/Str]
      :summary "List order status"
      (sdm-order-status/get-sdm-order-status authorization)) 
    
    (GET "/payment-options" []
      :header-params [authorization :- s/Str]
      :summary "List payment options"
      (sdm-payment-options/get-sdm-payment-options authorization)) 

    (GET "/shipping-services" []
      :header-params [authorization :- s/Str]
      :summary "List shipping service"
      (sdm-shipping/get-shipping-services authorization)) 

    (GET "/product-categories" []
      :header-params [authorization :- s/Str]
      :summary "List product categories"
      (sdm-product-categories/get-sdm-product-categories authorization)) 
    
    (POST "/products-images" []
      :body [sdm-products-images Sdm-products-images]
      :header-params [authorization :- s/Str]
      :summary "Add images url per product"
      (sdm-products-images/add-sdm-products-images authorization
                                                  (get sdm-products-images :url)
                                                  (get sdm-products-images :product-id))) 
    
    (POST "/get-products-images" []
      :body [sdm-products-images Sdm-products-images]
      :header-params [authorization :- s/Str]
      :summary "Add images url per product"
      (sdm-products-images/get-sdm-products-images-by-product-id authorization
                                                                (get sdm-products-images :product-id)))
    
    (GET "/payment-options" []
      :header-params [authorization :- s/Str]
      :summary "List shipping-services"
      (sdm-shipping/get-shipping-services authorization)) 

    (GET "/weight-options" []
      :header-params [authorization :- s/Str]
      :summary "List shipping-services"
      (sdm-weight/get-sdm-weight-options authorization))) 

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

    (POST "/resend-code" []
        :body [phone Phone]
        :summary "Enter correct phone number and received confirmation code"
        (register/resend-code (get phone :phone)))

     (POST "/loginbyemail" []
       :body [user User-mail]
       :summary "Login with email address get back token"
       (login/loginbyemail (get user :email) (get user :password)))

    ;  partnerlogin
     (POST "/partnerlogin" []
       :body [user User-mail]
       :summary "Login with email address get back token, Only partner are allowed"
       (partnerlogin/partner-login (get user :email) (get user :password)))

     (POST "/loginbyphone" []
       :body [user User-phone]
       :summary "Login with phone number get back token"
       (login/loginbyphone (get user :phone) 
                            (get user :password)))

     (POST "/userprofile" []
       :header-params [authorization :- s/Str]
       :body [profile User-profile]
       :summary "setup user profile information"
       (userinfo/setup-profile! authorization
                                (get profile :first_name)
                                (get profile :mid_name)
                                (get profile :last_name)
                                (get profile :gender)))
                              
     (POST "/add-phonenumber"  []
      :header-params [authorization :- s/Str]
      :body [phone Phone]
      :summary "Add phone number to existing user to verify"
      (userinfo/add-phone-number authorization
                         (get phone :phone)))

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
       (pay/get-portforlio authorization))

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
; Customer Loyalty Program

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
                               (get branches :approval_code)
                               (get branches :logo_uri)))

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
                                  (get update-branches :logo_uri)
                                  (get update-branches :is_active)))
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

     (POST "/set-qr" []
       :header-params [authorization :- s/Str]
       :body [genqr Genqr]
       :summary "Send data to request QR code"
       (genreward/gen-str authorization
                          (get genqr :location)
                          (get genqr :approval_code)
                          (get genqr :receipt_no)
                          (get genqr :amount)))

     (POST "/get-rewards" []
       :header-params [authorization :- s/Str]
       :body [getreward Getreward]
       :summary "Send qr to get rewards!"
       (genreward/valid-str? authorization
                             (get getreward :hashs)))

     (GET "/get-receipt" []
       :header-params [authorization :- s/Str]
       :summary "List all receipt activity"
       (receipts/get-receipt authorization))

     (POST "/branches-granted" []
       :header-params [authorization :- s/Str]
       :body [branches-granted Branches-granted]
       :summary "Enter the Branches Name and granted user to be able to manage the branches"
       (branches/granted-user! authorization
                               (get branches-granted :branches_name)
                               (get branches-granted :email)))

     (GET "/branches-granted" []
       :header-params [authorization :- s/Str]
       :summary "Get list all greanted branches"
       (branches/get-branches-by-granted authorization))

     (GET "/branches-created-by" []
       :header-params [authorization :- s/Str]
       :summary "Show created by only"
       (branches/get-branches-created-by authorization))

     (GET "/get-transactions-report" []
       :header-params [authorization :- s/Str]
       :summary "List the tranaction by branches"
       (receipts/get-reports authorization))

     (POST "/reports-from-to-date" []
       :header-params [authorization :- s/Str]
       :body [reports-from-to-date Reports-from-to-date]
       :summary "List the tranaction Between date"
       (receipts/get-reports-from-to-date authorization
                                          (get reports-from-to-date :from_date)
                                          (get reports-from-to-date :to_date)))

     (POST "/reports-from-to-date-by-location" []
       :header-params [authorization :- s/Str]
       :body [reports-from-to-date-by-location Reports-from-to-date-by-location]
       :summary "List the tranaction Between date"
       (receipts/get-trx-from-to-date-by-location authorization
                                                  (get reports-from-to-date-by-location :from_date)
                                                  (get reports-from-to-date-by-location :to_date)
                                                  (get reports-from-to-date-by-location :location)))

     (POST "/reports-by-location" []
       :header-params [authorization :- s/Str]
       :body [reports-by-location Reports-by-location]
       :summary "List the tranaction Between date"
       (receipts/get-trx-by-location authorization
                                     (get reports-by-location :location)))
    ;  ------------ END of Customer Loyalty----------------

     (POST "/forget-password-by-email" []
       :summary "Input User email address to received reseting code"
       :body [email Email]
       (login/forget-password-by-mail (get email :email)))

     (POST "/forget-password" []
       :summary "Input User phone number to received reseting code"
       :body [phone Phone]
       (login/forget-password (get phone :phone)))

     (POST "/reset-password" []
       :summary "Enter a valid reseting code and new password"
       :body [reset-password Reset-password]
       (login/reset-password! (get reset-password :temp_code)
                              (get reset-password :phone)
                              (get reset-password :password)))

     (POST "/reset-password-by-email" []
       :summary "Enter a valid reseting cod and new password"
       :body [reset-password-by-email Reset-password-by-email]
       (login/reset-password-by-mail! (get reset-password-by-email :temp_code)
                                      (get reset-password-by-email :email)
                                      (get reset-password-by-email :password)))

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
     (POST "/set-kyc" []
       :header-params [authorization :- s/Str]
       :summary "Submit KYC document to be approve by institution"
       :body [set-kyc Set-kyc]
       (userinfo/set-kyc! authorization
                          (get set-kyc :nationality)
                          (get set-kyc :occupation)
                          (get set-kyc :address)
                          (get set-kyc :document_no)
                          (get set-kyc :documenttype_id)
                          (get set-kyc :document_uri)
                          (get set-kyc :face_uri)
                          (get set-kyc :issue_date)
                          (get set-kyc :expire_date)))

     (GET "/get-documenttype" []
       :header-params [authorization :- s/Str]
       :summary "Get list of document type"
       (userinfo/get-documenttype authorization))

     (POST "/wallet-lookup" []
       :header-params [authorization :- s/Str]
       :summary "Lookup wallet by phone number"
       :body [wallet-lookup Wallet-lookup]
       (walletlookup/get-wallet authorization
                                (get wallet-lookup :phone)))

     (POST "/invite-phonenumber" []
      :header-params [authorization :- s/Str]
      :summary "Inviting people to join throug phone number"
      :body [phone Phone]
      (register/invite-phone-number authorization
                                    (get phone :phone)))
; next
     )))

(def handler
  (wrap-cors app :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :post]))