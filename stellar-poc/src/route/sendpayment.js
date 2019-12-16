const SendPayment = require("../op/Payment");

exports.sendpayment = async ctx => {
  try {
    await SendPayment(
      ctx.request.body.senderKey,
      ctx.request.body.assetCode,
      ctx.request.body.destination,
      ctx.request.body.amount,
      ctx.request.body.memo
    )
      .then(async doc => {
        ctx.status = 200;
        ctx.body = { message: `${doc}` };
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
