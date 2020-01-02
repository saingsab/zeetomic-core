const fetch = require("node-fetch");
const dotenv = require("dotenv");

dotenv.config();

async function GetIssuer(_assetCode, _wallet) {
  response = await fetch(`${process.env.HORIZONNET}/accounts/${_wallet}`);
  const payload = await response.json();
  if (payload.status == 404) {
    return `Error ${payload.title}`;
  }
  for (i = 0; i < payload.balances.length; i++) {
    if (payload.balances[i].asset_code === _assetCode) {
      return {
        issuer: payload.balances[i].asset_issuer,
        balance: payload.balances[i].balance
      };
    }
  }
}

module.exports = GetIssuer;
