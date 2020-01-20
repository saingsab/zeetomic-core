const StellarSdk = require("stellar-sdk");
const fetch = require("node-fetch");
const dotenv = require("dotenv");

dotenv.config();

const server = new StellarSdk.Server(process.env.HORIZONNET);

// Keys for accounts to issue and receive the new asset
var issuingKeys = StellarSdk.Keypair
  .fromSecret('SBBDIWCGZVDBV7EGMD7FCZMGXKVB4J2V276I5LMNL3BLEXBWEBIIMPRP');
var receivingKeys = StellarSdk.Keypair
  .fromSecret('SAZRJK5IYRHHUZA2XQDVZNR3FXKLSZK5N67BLSAHGDRWWQXRHYVZBXQB');

// Create an object to represent the new asset
var astroDollar = new StellarSdk.Asset('AyoungSD', issuingKeys.publicKey());

// First, the receiving account must trust the asset
server.loadAccount(receivingKeys.publicKey())
  .then(function(receiver) {
    var transaction = new StellarSdk.TransactionBuilder(receiver, {
      fee: 100,
      networkPassphrase: StellarSdk.Networks.TESTNET
    })
      // The `changeTrust` operation creates (or alters) a trustline
      // The `limit` parameter below is optional
      .addOperation(StellarSdk.Operation.changeTrust({
        asset: astroDollar,
        limit: '1000'
      }))
      // setTimeout is required for a transaction
      .setTimeout(100)
      .build();
    transaction.sign(receivingKeys);
    return server.submitTransaction(transaction);
  })
  .then(console.log)

  // Second, the issuing account actually sends a payment using the asset
  .then(function() {
    return server.loadAccount(issuingKeys.publicKey())
  })
  .then(function(issuer) {
    var transaction = new StellarSdk.TransactionBuilder(issuer, {
      fee: 100,
      networkPassphrase: StellarSdk.Networks.TESTNET
    })
      .addOperation(StellarSdk.Operation.payment({
        destination: receivingKeys.publicKey(),
        asset: astroDollar,
        amount: '10'
      }))
      // setTimeout is required for a transaction
      .setTimeout(100)
      .build();
    transaction.sign(issuingKeys);
    return server.submitTransaction(transaction);
  })
  .then(console.log)
  .catch(function(error) {
    console.error('Error!', error);
  });