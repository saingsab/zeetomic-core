// Load the AWS SDK for Node.js
var AWS = require("aws-sdk");
// Set region
AWS.config.update({
  accessKeyId: "AKIAV2YJRM5FKNODTNZ3",
  secretAccessKey: "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",
  region: "ap-northeast-1",
  apiVersion: "2006-03-01"
});

exports.sendsms = async ctx => {
  console.log(ctx.request.body.smscontent);
  // Create publish parameters
  var params = {
    Message: ctx.request.body.smscontent /* required */,
    PhoneNumber: ctx.request.body.phonenumber,
    MessageAttributes: {
      "AWS.SNS.SMS.SenderID": {
        DataType: "String",
        StringValue: "ZEETOMIC"
      }
    }
  };

  // Create promise and SNS service object
  var SendSMS = new AWS.SNS({ apiVersion: "2010-03-31" })
    .publish(params)
    .promise();

  // Handle promise's fulfilled/rejected states
  try {
    await SendSMS.then(function(data) {
      console.log("MessageID is " + data.MessageId);
      ctx.status = 200;
      ctx.body = { message: "SMS sent successfully" };
      return;
    }).catch(function(err) {
      console.error(err, err.stack);
    });
  } catch (e) {
    ctx.status = 200;
    ctx.body = { message: `ERROR ${e.message}` };
    return;
  }
};
