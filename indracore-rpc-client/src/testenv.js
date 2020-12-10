const dotenv = require("dotenv");

dotenv.config();
console.log(`Indracore-rpc-client Started at http://localhost:${process.env.SELPORT}`);