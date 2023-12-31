// Required imports
const { ApiPromise, WsProvider } = require('@polkadot/api');
const { Keyring } = require('@polkadot/keyring');

exports.transfer = async ctx => {
    const dest = ctx.request.body.dest;
    const sender = ctx.request.body.sender;
    const amount = ctx.request.body.amount;

    // Initialise the provider to connect to the local node 
    const provider = new WsProvider('wss://rpc-testnet.selendra.org');

    // Create the API and wait until ready
    const api = await ApiPromise.create({ provider });

    // Constuct the keying after the API (crypto has an async init)
    const keyring = new Keyring({ type: 'sr25519' });

    const senderKey = keyring.addFromMnemonic(sender);

    // Get Chain Decimalse from node
    const decimals = api.registry.chainDecimals;

    // Create a extrinsic, transferring amount units to dest in xx amount
    const transfer = api.tx.balances.transfer(dest, (BigInt(amount * (10 ** decimals))));

    // Sign and send the transaction using our account
    const hash = await transfer.signAndSend(senderKey);

    try {
        ctx.status = 200;
        ctx.body = { message: `${hash.toHex()}` };
        return;

    } catch (e) {
        ctx.status = 200;
        console.log(`ERROR ${e.message}`);
        ctx.body = { message: `ERROR ${e.message}` };
        return;
    }
}