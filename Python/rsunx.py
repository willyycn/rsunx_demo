# coding=utf-8
import requests
import urllib.parse
class RSunX:
    app_url = "http://127.0.0.1:2080"
    rsunx_path = "/hades"
    api_pwd = "38148a57d6c7f32c"
    def __init__(self):
        print("RSunX api")
    def getRand(self):
        return RSunX.client_get(self, "c")
    def getParam(self):
        return RSunX.client_get(self, "p")
    def getKeySeed(self,token, passwd, element):
        post_data = {"token": token, "passwd": passwd, "element": element}
        return RSunX.client_post(self, "r", post_data)
    def getSignature(self,challenge, check):
        post_data = {"challenge": challenge, "check": check}
        return RSunX.client_post(self, "s", post_data)
    def getVerify(self,sign, challenge, token, passwd, check):
        post_data = {"sign": sign,"challenge": challenge,"token": token, "passwd": passwd, "check": check}
        return RSunX.client_post(self, "v", post_data)
    def getEncrypt(self,plain, token, passwd, check):
        post_data = {"plain": plain,"token": token, "passwd": passwd, "check": check}
        return RSunX.client_post(self, "e", post_data)
    def getDecrypt(self,cipher):
        post_data = {"cipher": cipher}
        return RSunX.client_post(self, "d", post_data)
    def client_post(self,cmd,post_data):
        post_data['api_pwd'] = RSunX.api_pwd
        url = RSunX.app_url + RSunX.rsunx_path
        querystring = {"cmd": cmd}
        payload = urllib.parse.urlencode(post_data)
        headers = {
            'Content-Type': "application/x-www-form-urlencoded"
        }
        response = requests.request("POST", url, data=payload, headers=headers, params=querystring)
        return response.json()
    def client_get(self,cmd):
        url = RSunX.app_url + RSunX.rsunx_path
        querystring = {"cmd": cmd}
        headers = {
            'Content-Type': "application/json"
        }
        response = requests.request("GET", url, headers=headers, params=querystring)
        return response.json()