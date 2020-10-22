const { Keyring } = require('@polkadot/keyring');
const { cryptoWaitReady, mnemonicGenerate } = require('@polkadot/util-crypto');

exports.getwallet = async ctx => {
    await cryptoWaitReady();
    const keyring = new Keyring();
    const _mnemonic = mnemonicGenerate();

    const wallet = keyring.createFromUri(_mnemonic, { name: 'sr25519' }, 'sr25519');

    try {
        ctx.status = 200;

        // ctx.body = { message: { mnemonic: _mnemonic, wallet: wallet.address } }
        ctx.body = {
            wallet: `${wallet.address}`,
            seed: `${_mnemonic}`
        }
        return;

    } catch (e) {
        ctx.status = 200;
        ctx.body = { message: `ERROR ${e.message}` };
        return;
    }
}
