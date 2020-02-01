const Koa = require("koa");
const _ = require("koa-route");
const cors = require("@koa/cors");
const bodyParser = require("koa-bodyparser");
const dotenv = require("dotenv");

//Router
const sms = require("./routes/sms");
const wallet = require("./routes/wallet");
const transfer = require("./routes/transfer");

// Koa Web Configuration
dotenv.config();
const App = new Koa();
// const router = new Route();
App.use(cors({ origin: "*" }));
App.use(bodyParser());

App.use(_.post("/sms", sms.sms));
App.use(_.post("/wallet", wallet.wallet));
App.use(_.post("/transfer", transfer.transfer));
// Server 
try {
    App.listen(process.env.PORT);
    console.log(`Waves Server Started at http://localhost:${process.env.PORT}`);
  } catch (e) {
    console.log(e);
  }
  