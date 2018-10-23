/**
 * Created by willyy on 2017/3/1.
 */
let express = require('express');
let router = express.Router();
let rsunx = require('../core/rsunx');

router.use(function (req, res, next) {
    next();
});

router.get('/text', function (req, res) {
    res.json({
        ok:1
    });
})

let g_rand; //实际要使用缓存, 将token和rand联系起来

router.post('/generate', function (req, res) {
    let element = req.body.element;
    let token = req.body.token;
    let passwd = req.body.passwd;
    let challenge = req.body.challenge;
    rsunx.getDecrypt(token, function (status, plain, err) {
        if (status == 0){
            rsunx.getKeySeed(plain,passwd, element, function (status,seed,err) {
                if (status == 0){
                    rsunx.getSignature(challenge,"challenge",function (status, sign, err) {
                        if (status == 0){
                            rsunx.getRand(function (rand) {
                                g_rand = rand;
                                res.json({
                                    seed:seed,
                                    rand:g_rand,
                                    signature:sign,
                                    status:1
                                });
                            })
                        }
                    })
                }
            })
        }
    })
})

router.post("/verify", function (req, res) {
    let sign = req.body.sign;
    let token = req.body.token;
    let passwd = req.body.passwd;
    let check = req.body.check;
    rsunx.getVerify(sign,g_rand,token,passwd,check, function (status, verify, err) {
        if (status == 0){
            res.json({
                verify:verify
            })
        }
    })
});

module.exports = router;