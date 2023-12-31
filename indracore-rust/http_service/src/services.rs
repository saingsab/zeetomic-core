use actix_web::{web, HttpResponse, Result};
use cli::{
    balances::{check_balance, run_transaction, transfer_balance},
    models::{PublicAddress, SendTx, Transaction, Wallet},
    wallet::get_wallet,
};

use crate::errors::CustomeError;

pub async fn http_get_wallet(wl: web::Json<Wallet>) -> Result<HttpResponse, CustomeError> {
    let wallet = Wallet {
        label: wl.label.clone(),
        name: wl.name.clone(),
        location: wl.location.clone(),
        phrase: wl.phrase.clone(),
        password: wl.password.clone(),
    };
    match get_wallet(wallet) {
        Ok(res) => Ok(HttpResponse::Ok().json(res)),
        Err(e) => Err(CustomeError { error: e }),
    }
}

pub async fn http_check_balance(
    id: web::Json<PublicAddress>,
) -> Result<HttpResponse, CustomeError> {
    match check_balance(id.address.clone()) {
        Ok(res) => Ok(HttpResponse::Ok().json(res)),
        Err(e) => Err(CustomeError { error: e }),
    }
}

pub async fn http_transfer(tx: web::Json<Transaction>) -> Result<HttpResponse, CustomeError> {
    let transfer = Transaction {
        sender: tx.sender.clone(),
        receiver: tx.receiver.clone(),
        amount: tx.amount.clone(),
        location: tx.location.clone(),
    };
    match run_transaction(transfer).await {
        Ok(res) => Ok(HttpResponse::Ok().json(res)),
        Err(e) => {
            let v: Vec<&str> = e
                .split(|c| c == '{' || c == '}' || c == ':' || c == '"' || c == '(' || c == ')')
                .collect();
            let err = format!("{} {}, {}", v[8], v[13], v[14]);
            Err(CustomeError { error: err })
        }
    }
}

pub async fn http_pharse_transfer(tx: web::Json<SendTx>) -> Result<HttpResponse, CustomeError> {
    let transfer = SendTx {
        sender: tx.sender.clone(),
        receiver: tx.receiver.clone(),
        amount: tx.amount.clone(),
    };
    match transfer_balance(transfer).await {
        Ok(res) => Ok(HttpResponse::Ok().json(res)),
        Err(e) => Ok(HttpResponse::Ok().json(e)),
        // Err(e) => Err(CustomeError { error: e }),
    }
}
