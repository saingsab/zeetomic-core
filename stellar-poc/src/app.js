const Koa = require("koa");
const _ = require("koa-route");
const cors = require("@koa/cors");
const bodyParser = require("koa-bodyparser");
const dotenv = require("dotenv");
// Include Route
const createwallet = require("./route/createwallet");
const acceptasset = require("./route/acceptasset");
const sendpayment = require("./route/sendpayment");
const feecharge = require("./route/feecharge");
const sendsms = require("./route/sendsms");

dotenv.config();
const App = new Koa();
// const router = new Route();
App.use(cors({ origin: "*" }));
App.use(bodyParser());
App.use(_.post("/createwallet", createwallet.createwallet));
App.use(_.post("/acceptasset", acceptasset.acceptasset));
App.use(_.post("/sendpayment", sendpayment.sendpayment));
App.use(_.post("/feecharge", feecharge.feecharge));
App.use(_.post("/sendsms", sendsms.sendsms));

try {
  App.listen(process.env.PORT);
  console.log(`Server Started at http://localhost:${process.env.PORT}`);
} catch (e) {
  console.log(e);
}
