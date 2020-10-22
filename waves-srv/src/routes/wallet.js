var WavesAPI = require('@waves/waves-api');
// change from TESTNET to MAINNET in production
const Waves = WavesAPI.create(WavesAPI.TESTNET_CONFIG);
const newConfig = {

    // The byte allowing to distinguish networks (mainnet, testnet, devnet, etc)
    networkByte: Waves.constants.TESTNET_BYTE,

    // Node and Matcher addresses, no comments here
    nodeAddress: `${process.env.NODESRV}`,
    matcherAddress: `${process.env.NODESRV}/matcher`,

    // If a seed phrase length falls below that value an error will be thrown
    minimumSeedLength: 50

};
Waves.config.set(newConfig);

exports.wallet = async ctx => {
    try {
        const seed = Waves.Seed.create();
        ctx.status = 200;
        ctx.body = { wallet: seed.address, seed: seed.phrase };
        return;
    } catch (e) {
        ctx.status = 200;
        ctx.body = { message: `ERROR ${e.message}` };
        return;
    }
}
