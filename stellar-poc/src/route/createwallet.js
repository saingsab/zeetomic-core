const StellarSdk = require("stellar-sdk");
const CreateWallet = require("../op/CreateWallet");

exports.createwallet = async ctx => {
  const pair = StellarSdk.Keypair.random();
  const wallets = { wallet: pair.publicKey(), seed: pair.secret() };
  try {
    await CreateWallet(ctx.request.body.dkey, wallets)
      .then(async doc => {
        ctx.status = 200;
        ctx.body = {
          wallet: `${wallets.wallet}`,
          seed: `${wallets.seed}`
        };
        return;
      })
      .catch(err => {
        ctx.status = 200;
        ctx.body = { message: `${err.message}` };
        return;
      });
  } catch (e) {
    ctx.status = 200;
    ctx.body = { message: `${e.message}` };
    return;
  }
};
