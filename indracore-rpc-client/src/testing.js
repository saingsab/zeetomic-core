// Import the API, Keyring and some utility functions
const { ApiPromise, WsProvider } = require('@polkadot/api');
const { Keyring } = require('@polkadot/keyring');

const BOB = '5Fnck89ATdAiZ4eS8G66Q948F38UbkcQU4vPfwCjwxQNt1is';
const amount = 10;

async function main() {
    // Instantiate the API

    // Initialise the provider to connect to the local node
    const wsProvider = new WsProvider('wss://rpc-testnet.selendra.org');
    const api = await ApiPromise.create({ provider: wsProvider });

    // Constuct the keying after the API (crypto has an async init)
    const keyring = new Keyring({ type: 'sr25519' });

    // Add Alice to our keyring with a hard-deived path (empty phrase, so uses dev)
    const alice = keyring.addFromMnemonic('urban borrow silly mass orange around brand bread exile refuse diesel fantasy');

    // Create a extrinsic, transferring 12345 units to Bob
    const transfer = api.tx.balances.transfer(BOB, (BigInt(amount * (10 ** 18))));

    // Sign and send the transaction using our account
    const hash = await transfer.signAndSend(alice);

    console.log('Transfer sent with hash', hash.toHex());
}

main().catch(console.error).finally(() => process.exit());