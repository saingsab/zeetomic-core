const StellarSdk = require("stellar-sdk");
const dotenv = require("dotenv");

dotenv.config();

const server = new StellarSdk.Server(process.env.HORIZONNET);
eval(`var NETWORK_PASSPHRASE = ${process.env.NETWORK_PASSPHRASE}`);

async function AcceptAsset(accountKey, _AssetCode, _assetIssuer) {
  const accountKeypair = StellarSdk.Keypair.fromSecret(accountKey);
  const [{ p90_accepted_fee: fee }, account] = await Promise.all([
    server.feeStats(),
    server.loadAccount(accountKeypair.publicKey())
  ]);

  const changeTrustTx = new StellarSdk.TransactionBuilder(account, {
    fee: 100,
    networkPassphrase: NETWORK_PASSPHRASE
  })
    .addOperation(
      StellarSdk.Operation.changeTrust({
        asset: new StellarSdk.Asset(_AssetCode, _assetIssuer)
      })
    )
    .setTimeout(30)
    .build();

  changeTrustTx.sign(accountKeypair);
  console.log(`Creating trustline and issuing...`);
  const txResult = await server.submitTransaction(changeTrustTx);
  console.log(txResult.hash);
}

module.exports = AcceptAsset;
