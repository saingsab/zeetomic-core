
const { ApiPromise, WsProvider } = require('@polkadot/api');

exports.balances = async ctx => {
    const ADDR = ctx.request.body.add;

    // Initialise the provider to connect to the local node
    //const provider = new WsProvider('wss://rpc-testnet.selendra.org');
    const provider = new WsProvider('ws://127.0.0.1:9944');

    // Create the API and wait until ready
    const api = await ApiPromise.create({ provider });

    // Retrieve the last timestamp
    const now = await api.query.timestamp.now();

    // Retrieve the account balance & nonce via the system module
    const { data: balance } = await api.query.system.account(ADDR);
    const decimals = api.registry.chainDecimals;

    // console.log(`${now}: balance of ${balance.free / (10 ** 18)} and a nonce of ${nonce}`);
    try {
        ctx.status = 200;
        ctx.body = {
            data: {
                timestamp: `${now}`,
                balance: `${balance.free / (10 ** decimals)}`,
                otherassets: 'balance on other contract'
            }
        }
        return;
    } catch (e) {
        ctx.status = 200;
        ctx.body = { message: `ERROR ${e.message} ` };
        return;
    }
}