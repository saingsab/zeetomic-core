use actix_web::{web, App, HttpServer};

pub mod errors;
pub mod services;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .route("/wallet", web::post().to(services::http_get_wallet))
            .route("/balance", web::post().to(services::http_check_balance))
            .route("/transfer", web::post().to(services::http_pharse_transfer))
    })
    .bind(("0.0.0.0", 9002))?
    .run()
    .await
}
