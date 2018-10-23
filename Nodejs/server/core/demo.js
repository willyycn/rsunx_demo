/**
 * Created by willyy on 2017/2/22.
 */
let express = require('express');

let bodyParser = require('body-parser');
let api = require('../demo/demo_1_0_0');
let demo = express();
demo.use(bodyParser.json());
demo.use(bodyParser.urlencoded({ extended: false }));
demo.use('/demo',api);

// catch 404 and forward to error handler
demo.use(function(req, res, next) {
    let err = new Error('Not Found');
    err.message = "api: "+req.originalUrl + " method: "+req.method+" is not found; ";
    err.status = 404;
    next(err);
});
demo.use(function(err, req, res, next) {
    // set locals, only providing error in development
    res.json({
        message:err.message,
        stack:err
    });
});
module.exports = demo;
