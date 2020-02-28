/** @format */

const FeeCharge = require("../op/FeeCharge");

exports.feecharge = async ctx => {
  try {
    await FeeCharge(ctx.request.body.seed)
      .then(async Feecharge => {
        ctx.status = 200;
        ctx.body = { message: true };
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
