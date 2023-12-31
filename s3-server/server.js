/** @format */

const Koa = require("koa");
const _ = require("koa-route");
const cors = require("@koa/cors");
const bodyParser = require("koa-bodyparser");
const json = require("koa-json");
const koaBody = require("koa-body");
const uploadFile = require("./uploadFile");
const uuid = require("uuid/v4");
const dotenv = require("dotenv");

dotenv.config();
const App = new Koa();
App.use(cors({ origin: "*" }));

App.use(bodyParser());

App.use(json());

// Enable multipart body parsing
App.use(koaBody({ multipart: true }));

App.use(
  // problic Profile
  _.post("/upload", async (ctx) => {
    const file = ctx.request.files.file;
    const { key } = await uploadFile({
      bucket: "selendra",
      acl: "public-read",
      fileName: uuid().toString(),
      filePath: file.path,
      fileType: file.type,
    });
    ctx.body = {
      uri: `https://selendra.s3-ap-southeast-1.amazonaws.com/${key}`
    };
  })
);

App.listen(process.env.S3PORT);
console.log(`S3 Server started at ${process.env.S3PORT}`);
