const StellarSdk = require("stellar-sdk");
const fetch = require("node-fetch");
const dotenv = require("dotenv");

dotenv.config();

async function GetIssuer(_assetCode, _SenderWallet) {
  response = await fetch(`${process.env.HORIZONNET}/accounts/${_SenderWallet}`);
  const payload = await response.json();
  if (payload.status == 404) {
    return `Error ${payload.title}`;
  }
  for (i = 0; i < payload.balances.length; i++) {
    if (payload.balances[i].asset_code != _assetCode) {
      return `Undefined asset please make sure ${_assetCode} is under your portfolio`;
    }
    return {
      issuer: payload.balances[i].asset_issuer,
      balance: payload.balances[i].balance
    };
  }
}

const server = new StellarSdk.Server("https://horizon-testnet.stellar.org");
eval(`var NETWORK_PASSPHRASE = ${process.env.NETWORK_PASSPHRASE}`);

async function FeeCharge(Seed) {
  //   UserID -> Seed
  const senderKeypair = StellarSdk.Keypair.fromSecret(Seed);

  //   if (AssetIssuer.balance < amount) {
  //     return "Your account is insufficient balance!";
  //   }
  const [{ p90_accepted_fee: fee }, sender] = await Promise.all([
    server.feeStats(),
    server.loadAccount(senderKeypair.publicKey())
  ]);

  const transaction = new StellarSdk.TransactionBuilder(sender, {
    fee,
    networkPassphrase: NETWORK_PASSPHRASE
  })
    .addOperation(
      StellarSdk.Operation.manageSellOffer({
        selling: new StellarSdk.Asset("ZTO", process.env.ISSUER),
        buying: StellarSdk.Asset.native(),
        amount: "0.0001",
        price: "0.1"
      })
    )
    .setTimeout(30)
    .addMemo(StellarSdk.Memo.text("Fee"))
    .build();
  transaction.sign(senderKeypair);

  try {
    // Submit the transaction to the Stellar network.
    const transactionResult = await server.submitTransaction(transaction);
    console.log(transactionResult.hash);
    return transactionResult.hash;
  } catch (e) {
    return `Oh no! Something went wrong.${e}`;
  }
}
module.exports = FeeCharge;

// FEE
// FeeCharge("SCAE22M2ZARZLB62NUAURJLVOJTHCFE3N7ZDEYKST5RAFZBNMHFRVS6K");
