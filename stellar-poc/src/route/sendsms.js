/** @format */

// // Load the AWS SDK for Node.js
// var AWS = require("aws-sdk");
// // Set region
// AWS.config.update({
//   accessKeyId: "AKIAV2YJRM5FKNODTNZ3",
//   secretAccessKey: "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",
//   region: "ap-southeast-1",
//   apiVersion: "2006-03-01"
// });

// exports.sendsms = async ctx => {
//   console.log(ctx.request.body.smscontent);
//   // Create publish parameters
//   var params = {
//     Message: ctx.request.body.smscontent /* required */,
//     PhoneNumber: ctx.request.body.phonenumber,
//     MessageAttributes: {
//       "AWS.SNS.SMS.SenderID": {
//         DataType: "String",
//         StringValue: "ZEETOMIC"
//       }
//     }
//   };

//   // Create promise and SNS service object
//   var SendSMS = new AWS.SNS({ apiVersion: "2010-03-31" })
//     .publish(params)
//     .promise();

//   // Handle promise's fulfilled/rejected states
//   try {
//     await SendSMS.then(function(data) {
//       console.log("MessageID is " + data.MessageId);
//       ctx.status = 200;
//       ctx.body = { message: "SMS sent successfully" };
//       return;
//     }).catch(function(err) {
//       console.error(err, err.stack);
//     });
//   } catch (e) {
//     ctx.status = 200;
//     ctx.body = { message: `ERROR ${e.message}` };
//     return;
//   }
// };

const accountSid = "ACf9a21024405643be5e14103572eefca5";
const authToken = "5c3b92fb41ac3c1ce4152e92d5ceed75";
const client = require("twilio")(accountSid, authToken);

exports.sendsms = async ctx => {
  try {
    client.messages
      .create({
        body: ctx.request.body.smscontent,
        from: "+12032049810",
        to: ctx.request.body.phonenumber
      })
      .then(message => {
        console.log(message.sid);
      })
      .done();
    ctx.status = 200;
    ctx.body = { message: `SUCCE : Sent to ${ctx.request.body.phonenumber}` };
    return;
  } catch (e) {
    ctx.status = 200;
    ctx.body = { message: `ERROR ${e.message}` };
    return;
  }
};
