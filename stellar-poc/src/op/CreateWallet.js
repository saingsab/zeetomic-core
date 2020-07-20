const StellarSdk = require("stellar-sdk");
const dotenv = require("dotenv");

dotenv.config();
const server = new StellarSdk.Server(process.env.HORIZONNET);
eval(`var NETWORK_PASSPHRASE = ${process.env.NETWORK_PASSPHRASE}`);
async function CreateWallet(DisKey, wallets) {
  let destination = wallets.wallet;
  let memo = "Account Activation Rewarded";
  const senderKeypair = StellarSdk.Keypair.fromSecret(DisKey);
  const [{ p90_accepted_fee: fee }, sender] = await Promise.all([
    server.feeStats(),
    server.loadAccount(senderKeypair.publicKey())
  ]);

  const transaction = new StellarSdk.TransactionBuilder(sender, {
    fee: 100,
    // fee: process.env.FEE,
    networkPassphrase: NETWORK_PASSPHRASE
  })
    .addOperation(
      StellarSdk.Operation.createAccount({
        destination: destination,
        startingBalance: process.env.STARTBAL
      })
    )
    .setTimeout(30)
    .addMemo(StellarSdk.Memo.text(memo))
    .build();
  transaction.sign(senderKeypair);

  try {
    console.log("Submitting Transaction ...");
    const transactionResult = await server.submitTransaction(transaction);
    console.log(transactionResult.hash);
    return wallets;
  } catch (e) {
    return `Something went wrong.${e}`;
  }
}
module.exports = CreateWallet;
