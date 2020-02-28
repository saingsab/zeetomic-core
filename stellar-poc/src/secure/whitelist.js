/** @format */

const StellarSdk = require("stellar-sdk");
const dotenv = require("dotenv");
dotenv.config();

eval(`var NETWORK_PASSPHRASE = ${process.env.NETWORK_PASSPHRASE}`);
async function allowTrust(_trustorAcc, _assetCode) {
  const stellarServer = new StellarSdk.Server(process.env.HORIZONNET);

  try {
    const issuingKeys = StellarSdk.Keypair.fromSecret(process.env.ISSEC);
    const issuingAccount = await stellarServer.loadAccount(
      issuingKeys.publicKey()
    );

    const transaction = new StellarSdk.TransactionBuilder(issuingAccount, {
      fee: StellarSdk.BASE_FEE,
      networkPassphrase: NETWORK_PASSPHRASE
    })
      .addOperation(
        StellarSdk.Operation.allowTrust({
          trustor: _trustorAcc,
          assetCode: _assetCode,
          authorize: true
        })
      )
      .setTimeout(30)
      .build();

    transaction.sign(issuingKeys);

    const result = await stellarServer.submitTransaction(transaction);
    // writeLogs(`trust allowed ${result}`);
    console.log(`Successfully adding whitelist ${_trustorAcc}`);
    return `Successfully adding whitelist ${_trustorAcc}`;
  } catch (e) {
    // writeLogs(`allow trust failed ${e.message}`);
    console.log(`Error Adding whitelist was failed  ${e} `);
  }
}

exports.whitelist = async ctx => {
  try {
    ctx.status = 200;
    ctx.body = await allowTrust(
      ctx.request.body.trustoracc,
      ctx.request.body.assetcode
    );

    return;
  } catch (e) {
    console.log(`Error Adding whitelist was failed  ${e} `);
    return e;
  }
};
