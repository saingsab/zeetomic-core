extern crate env_logger;
use std::env;

use cli::{operation, operation::Cmd, balances, wallet};

#[async_std::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    env_logger::init();

    let args = env::args().collect();
    match operation::parse(args) {
        Ok(Cmd::Help(cmd)) => operation::print_usage(cmd),
        Ok(Cmd::Version) => operation::print_version(),
        Ok(Cmd::Balance(cmd)) => balances ::op_check_balance(cmd),
        Ok(Cmd::GetWallet(wallet)) => wallet::op_get_wallet(wallet),
        Ok(Cmd::ListWallet(ls)) => wallet::list_wallet(ls),
        Ok(Cmd::WatchOnly(wl)) => wallet::watch_wallet(wl),
        Ok(Cmd::Restore(rw)) => wallet::restore_wallet(rw),
        Ok(Cmd::Backup(bp)) => wallet::backup(bp),
        Ok(Cmd::Transaction(tx)) => balances ::op_run_transaction(tx).await,
        Err(msg) => {
            println!("{}", msg);
            std::process::exit(127);
        }
    };

    Ok(())
}
