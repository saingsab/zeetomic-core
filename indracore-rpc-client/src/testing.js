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

    // // Constuct the keying after the API (crypto has an async init)
    // const keyring = new Keyring({ type: 'sr25519' });

    // // Add Alice to our keyring with a hard-deived path (empty phrase, so uses dev)
    // const alice = keyring.addFromMnemonic('urban borrow silly mass orange around brand bread exile refuse diesel fantasy');

    // // Create a extrinsic, transferring 12345 units to Bob
    // const transfer = api.tx.balances.transfer(BOB, (BigInt(amount * (10 ** 18))));

    // // Sign and send the transaction using our account
    // const hash = await transfer.signAndSend(alice);

    // console.log('Transfer sent with hash', hash.toHex());
    // This example set shows how to make queries at a point
    const ALICE = '5GrwvaEF5zXb26Fz9rcQpDWS57CtERHpNehXCPcNoHGKutQY';

    // retrieve the balance, once-off at the latest block
    const [nonce, { free }] = await api.query.system.account(ALICE);

    console.log('Alice has a current balance of', free.toHuman());

    // retrieve balance updates with an optional value callback
    const balanceUnsub = await api.query.system.account(ALICE, ([, { free }]) => {
        console.log('Alice has an updated balance of', free.toHuman());
    });

    // retrieve the balance at a block hash in the past
    const header = await api.rpc.chain.getHeader();
    const prevHash = await api.rpc.chain.getBlockHash(header.blockNumber.subn(42));
    const [, { free: prev }] = await api.query.system.account.at(prevHash, ALICE);

    console.log('Alice had a balance of', prev.toHuman(), '(42 blocks ago)');

    // useful in some situations - the value hash and storage entry size
    const currHash = await api.query.system.account.hash(ALICE);
    const currSize = await api.query.system.account.size(ALICE);

    console.log('Alice account entry has a value hash of', currHash, 'with a size of', currSize);
}

main().catch(console.error).finally(() => process.exit());

// All code is wrapped within an async closure,
// allowing access to api, hashing, types, util.
// (async ({ api, hashing, types, util }) => {
//   ... any user code is executed here ...
// })();

