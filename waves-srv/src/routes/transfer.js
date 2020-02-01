const { transfer, broadcast } = require("@waves/waves-transactions");


exports.transfer = async ctx => {
    try {
      // "retreat sweet subway relax repeat giggle cross ceiling trouble brother coconut mind indoor into hold";
      const seed = ctx.request.body.seed
      const signedTranserTx = transfer(
        {
          amount: (ctx.request.body.amount * 100000000),
          recipient: ctx.request.body.recipient, //"3MwAZCvPLofPwx2X6nfkGBq4nQ6x6AhuyVr",
          assetId: ctx.request.body.assetId, // 'EXGCeHPMVKVrSHqbjPWZSA6QzThyF9Rz5hMAKqf3R1mc',
          feeAssetId: ctx.request.body.feeAssetId//  'EXGCeHPMVKVrSHqbjPWZSA6QzThyF9Rz5hMAKqf3R1mc'
          
        },
        seed
      );
      const nodeUrl = `${process.env.NODESRV}`;
      // Change the ChainId to W for Main Net
      broadcast({ ...signedTranserTx, chainId: `${process.env.CHAINID}` }, nodeUrl)
        .then(resp => console.log(resp))
        .catch(err => console.error(err));
      ctx.status = 200;
      ctx.body = { message: `ERROR ${e.message}` };
      return;
    }catch (e){
      ctx.status = 200;
      ctx.body = { message: `ERROR ${e.message}` };
      return;
    }
}