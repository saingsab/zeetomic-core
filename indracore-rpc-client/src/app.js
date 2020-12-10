const Koa = require("koa");
const _ = require("koa-route");
const cors = require("@koa/cors");
const bodyParser = require("koa-bodyparser");
const dotenv = require("dotenv");

dotenv.config();

// Router
const transfer = require("./routes/transfer");
const getwallet = require("./routes/getwallet");
const balances = require("./routes/balances");

// Koa Web Configuration
const App = new Koa();
// const router = new Route();
App.use(cors({ origin: "*" }));
App.use(bodyParser());

// Routing
App.use(_.post("/transfer", transfer.transfer));
App.use(_.post("/getwallet", getwallet.getwallet));
App.use(_.post("/balances", balances.balances));

// Server 
try {
    App.listen(process.env.SELPORT);
    console.log(`Indracore-rpc-client Started at http://localhost:${process.env.SELPORT}`);
}
catch (e) {
    console.log(e);
}
