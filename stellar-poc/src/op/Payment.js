const StellarSdk = require("stellar-sdk");
const GetIssuer = require("./GetIssuer");
const dotenv = require("dotenv");

dotenv.config();
const server = new StellarSdk.Server(process.env.HORIZONNET);
eval(`var NETWORK_PASSPHRASE = ${process.env.NETWORK_PASSPHRASE}`);

async function SendPayment(Seed, _assetCode, destination, amount, memo) {
  //   UserID -> Seed
  const senderKeypair = StellarSdk.Keypair.fromSecret(Seed);

  const AssetIssuer = await GetIssuer(
    _assetCode,
    senderKeypair.publicKey()
  ).catch(err => {
    throw err.message;
  });

  if (AssetIssuer.balance < amount) {
    return "Your account is insufficient balance!";
  }
  const [{ p90_accepted_fee: fee }, sender] = await Promise.all([
    server.feeStats(),
    server.loadAccount(senderKeypair.publicKey())
  ]);

  const transaction = new StellarSdk.TransactionBuilder(sender, {
    fee: 100,
    networkPassphrase: NETWORK_PASSPHRASE
  })
    .addOperation(
      StellarSdk.Operation.payment({
        destination: destination,
        asset: new StellarSdk.Asset(_assetCode, AssetIssuer.issuer),
        amount: amount
      })
    )
    .setTimeout(30)
    .addMemo(StellarSdk.Memo.text(memo))
    .build();
  transaction.sign(senderKeypair);

  try {
    // Submit the transaction to the Stellar network.
    const transactionResult = await server.submitTransaction(transaction);
    // console.log(transactionResult);
    console.log(transactionResult.hash);
    return transactionResult.hash, `Your payment is completed!`;
  } catch (e) {
    return `Oh no! Something went wrong.${e}`;
  }
}
module.exports = SendPayment;
