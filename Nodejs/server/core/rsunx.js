/**
 * Created by willyy on 2017/2/22.
 * 此脚本展示了如何集成rsun api. 在内网情况下, 业务服务器和R-Sun-Server不必使用双向认证,
 * 但是在其他环境下, 建议业务服务器和R-Sun-Server要建立双向认证, 具体如何建立双向认证可以查看相关文档.
 */

let app_url = "127.0.0.1",
    app_port = 2080;
let http = require('http');
let qs = require("querystring");
let rsun_path = "/hades?cmd=";
let api_pwd = "38148a57d6c7f32c";

/**
 * 获取系统随机数
 * @param callback (rand,error)
 */
exports.getRand = function(callback){
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'c',
        method: 'GET'
    }

    let req = http.request(options,function(res){
        res.on('data', function(d){
            let obj = JSON.parse(d);
            let rand = obj.rand;
            callback(rand);
        });
    });
    req.on('error',function(e){
        callback("",e);
    });
    req.end();
}

/**
 * 获取系统参数数
 * @param callback (param,error)
 */
exports.getParam = function(callback){
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'p',
        method: 'GET'
    }

    let req = http.request(options,function(res){
        res.on('data', function(d){

        });
        let data = '';
        res.on('data', function(d) {
            data+=d;
        });
        res.on('end', function () {
            try{
                let obj = JSON.parse(d);
                let para = obj.comm_param;
                let srv_token = obj.srv_token;
                let t_chk_sum = obj.t_chk_sum;
                callback(0,para,srv_token,t_chk_sum);
            }
            catch (e) {
                callback(1, e);
            }
        });
    });
    req.on('error',function(e){
        callback(1, "", e);
    });
    req.end();
}

/**
 * 根据token生成私钥
 * @param token
 * @param passwd
 * @param element 交互因子
 * @param callback (私钥, error)
 */
exports.getKeySeed = function(token, passwd, element, callback){
    let form = {
        token : token,
        passwd : passwd,
        api_pwd: api_pwd,
        element: element
    };
    let bodyString = qs.stringify(form);
    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Cache-Control': 'no-cache'
    };
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'r',
        method: 'POST',
        headers:  headers,
        form: form
    };

    let req = http.request(options, function(res) {
        let data = '';
        res.on('data', function(d) {
            data+=d;
        });
        res.on('end', function () {
            try{
                let obj = JSON.parse(data);
                callback(obj.status, obj.seed, obj.err);
            }
            catch (e) {
                callback(1, "", e);
            }
        });
    });
    req.write(bodyString);
    req.on('error', function(e) {
        callback(1,"", e);
    });
    req.end();
}

/**
 * 获取服务器对挑战信息的签名
 * @param challenge 挑战信息
 * @param check 挑战报头
 * @param callback(signature, error) 返回
 */
exports.getSignature = function(challenge, check, callback){
    let body = {
        challenge: challenge,
        check: check,
        api_pwd: api_pwd
    };
    let bodyString = qs.stringify(body);
    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Cache-Control': 'no-cache'
    }
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'s',
        method: 'POST',
        headers:  headers,
        form: body
    };
    let req = http.request(options, function(res) {
        let data = '';
        res.on('data', function(d) {
            data+=d;
        });
        res.on('end', function () {
            try{
                let obj = JSON.parse(data);
                callback(obj.status, obj.signature, obj.err);
            }
            catch (e) {
                callback(1, "", e);
            }
        });
    });
    req.write(bodyString);
    req.on('error', function(e) {
        callback(1,"", e);
    });
    req.end();
}

/**
 * 根据信息验签
 * @param token
 * @param signature
 * @param challenge
 * @param passwd
 * @param check token的checksum
 * @param callback (verify, error)
 */
exports.getVerify = function(signature, challenge, token, passwd, check, callback){
    let body = {
        token : token,
        sign : signature,
        challenge : challenge,
        passwd : passwd,
        check : check,
        api_pwd : api_pwd
    };
    let bodyString = qs.stringify(body);
    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Cache-Control': 'no-cache'
    };
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'v',
        method: 'POST',
        headers:  headers,
        form: body
    };

    let req = http.request(options, function(res) {
        res.on('data', function(d) {
            let obj = JSON.parse(d);
            callback(obj.status,obj.verify,"");
        });
    });
    req.write(bodyString);
    req.on('error', function(e) {
        callback(1, "", e);
    });
    req.end();
}

/**
 * 使用输入信息加密
 * @param plain
 * @param token
 * @param passwd
 * @param check token的checksum
 * @param callback
 */
exports.getEncrypt = function (plain, token, passwd, check, callback) {
    let body = {
        plain: plain,
        token : token,
        passwd : passwd,
        check : check,
        api_pwd : api_pwd
    };
    let bodyString = qs.stringify(body);
    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Cache-Control': 'no-cache'
    }
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'e',
        method: 'POST',
        headers:  headers,
        form: body
    };
    let req = http.request(options, function(res) {
        let data = '';
        res.on('data', function(d) {
            data+=d;
        });
        res.on('end', function () {
            try{
                let obj = JSON.parse(data);
                callback(obj.status, obj.cipher, obj.err);
            }
            catch (e) {
                callback(1, "", e);
            }
        });
    });
    req.write(bodyString);
    req.on('error', function(e) {
        callback(1,"", e);
    });
    req.end();
}

/**
 * 获取服务器解密明文
 * @param cipher 密文
 * @param callback
 */
exports.getDecrypt = function(cipher, callback){
    let body = {
        cipher : cipher,
        api_pwd : api_pwd
    };
    let bodyString = qs.stringify(body);
    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Cache-Control': 'no-cache'
    }
    let options = {
        host: app_url,
        port: app_port,
        path: rsun_path+'d',
        method: 'POST',
        headers:  headers,
        form: body
    };
    let req = http.request(options, function(res) {
        let data = '';
        res.on('data', function(d) {
            data+=d;
        });
        res.on('end', function () {
            try{
                let obj = JSON.parse(data);
                callback(obj.status, obj.plain, obj.err);
            }
            catch (e) {
                callback(1, "", e);
            }
        });
    });
    req.write(bodyString);
    req.on('error', function(e) {
        callback(1,"", e);
    });
    req.end();
}
