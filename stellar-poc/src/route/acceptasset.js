const AcceptAsset = require("../op/AcceptAsset");

exports.acceptasset = async ctx => {
  try {
    await AcceptAsset(
      ctx.request.body.akey,
      ctx.request.body.assetCode,
      ctx.request.body.assetIssuer
    )
      .then(async Acceptasset => {
        ctx.status = 200;
        ctx.body = {
          message: `You successfully added ${ctx.request.body.assetCode} into your portforilo`
        };
        return;
      })
      .catch(err => {
        ctx.status = 200;
        ctx.body = { message: `ERROR ${err.message}` };
        return;
      });
  } catch (e) {
    ctx.status = 200;
    ctx.body = { message: `ERROR ${e.message}` };
    return;
  }
};
