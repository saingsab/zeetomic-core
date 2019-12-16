const aws = require("aws-sdk");
const fs = require("fs");
const dotenv = require("dotenv");

dotenv.config();

const uploadFile = async ({ bucket, acl, fileName, filePath, fileType }) => {
  var s3 = new aws.S3({
    accessKeyId: process.env.IAMS3,
    secretAccessKey: process.env.IAMS3SEC,
    region: "ap-southeast-1",
    apiVersion: "2006-03-01"
  });
  const stream = fs.createReadStream(filePath);
  stream.on("error", function(err) {
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
      function(err, data) {
        dotenv;
        if (err) {
          return { key: "ERROR :", url: err.code };
        }
        return { key: data };
      }
    )
    .promise();
};
module.exports = uploadFile;
