const accountSid = 'ACf9a21024405643be5e14103572eefca5'; 
const authToken = '5c3b92fb41ac3c1ce4152e92d5ceed75'; 
const client = require('twilio')(accountSid, authToken); 

exports.sms = async ctx => {
    try {
        client.messages 
            .create({ 
                body: ctx.request.body.smscontent, 
                from: '+12032049810',       
                to: ctx.request.body.phonenumber 
            }) 
            .then(message => {
                console.log(message.sid)
            }) 
            .done();
            ctx.status = 200;
            ctx.body = { message: `SUCCE : Sent to ${ctx.request.body.phonenumber}` };
            return;
    }catch (e) {
        ctx.status = 200;
        ctx.body = { message: `ERROR ${e.message}` };
        return;
      }
}

