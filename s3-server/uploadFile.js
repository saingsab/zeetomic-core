const AWS = require("aws-sdk");
const fs = require("fs");
const dotenv = require("dotenv");

dotenv.config();

// Enter copied or downloaded access ID and secret key here
const ID = process.env.IAMS3;
const SECRET = process.env.IAMS3SEC;

// The name of the bucket that you have created
const BUCKET_NAME = process.env.BUCKET_NAME;

const s3 = new AWS.S3({
  accessKeyId: ID,
  secretAccessKey: SECRET
});

const uploadFile = async ({ bucket, acl, fileName, filePath, fileType }) => {
  // var s3 = new aws.S3({
  //   accessKeyId: process.env.IAMS3,
  //   secretAccessKey: process.env.IAMS3SEC,
  //   region: "ap-southeast-1",
  //   apiVersion: "2006-03-01"
  // });
  const stream = fs.createReadStream(filePath);
  stream.on("error", function (err) {
    console.log("FN: STREAM " + err);
    reject(err);
  });

  return s3
    .upload(
      {
        Bucket: bucket,
        ACL: acl,
        Body: stream,
        Key: fileName,
        ContentType: fileType
      },
      function (err, data) {
        dotenv;
        if (err) {
          return { key: "ERROR :", url: err.code };
        }
        // console.log(`File uploaded successfully. ${data.Location}`);
        return { key: data };
      }
    )
    .promise();
};
module.exports = uploadFile;
