/**
 * Created by willyy on 2018/10/22.
 * 此脚本展示了如何集成rsun api. 在内网情况下, 业务服务器和R-Sun-Server不必使用双向认证,
 * 但是在其他环境下, 建议业务服务器和R-Sun-Server要建立双向认证, 具体如何建立双向认证可以查看相关文档.
 */

package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"strconv"
	"strings"
)

var app_url = "http://127.0.0.1:2080"
var rsun_path = "/hades?cmd="
var api_pwd = "38148a57d6c7f32c"

func getRand()(res string,err error)  {
	b , err1 := client_Get(app_url+rsun_path+"c")
	if err1!=nil {
		fmt.Printf("getRand err : %s",err1)
		err = err1
	}
	var resp struct {
		Rand  string `json:"rand"`
	}
	err2 := json.Unmarshal(b, &resp)
	if err2 != nil {
		fmt.Printf(err2.Error())
		err = err2
	}
	challenge := resp.Rand
	return challenge ,err
}

func getParam()(status int, param string, srv_token string, t_chk string, v string, err error)  {
	b, err1 := client_Get(app_url+rsun_path+"p")
	if err1!=nil {
		fmt.Println("getParam err : %s",err1)
		err = err1
		return
	}
	var resp struct{
		Status int `json:"status"`
		Comm_param string `json:"comm_param"`
		Srv_token string `json:"srv_token"`
		T_chk_sum string `json:"t_chk_sum"`
		Version string `json:"version"`
	}
	err2 := json.Unmarshal(b, &resp)
	if err2!=nil{
		fmt.Printf(err2.Error())
		err = err2
		return
	}
	return resp.Status, resp.Comm_param, resp.Srv_token, resp.T_chk_sum, resp.Version, nil
}

func getKeySeed(token string, passwd string, element string)(status int, seed string, err error)  {
	data := url.Values{}
	data.Set("token", token)
	data.Set("passwd", passwd)
	data.Set("element", element)
	data.Set("api_pwd", api_pwd)

	b, err2 := client_Post(app_url+rsun_path+"r", data.Encode())
	if err2 != nil {
		fmt.Printf(err2.Error())
		err = err2
		return
	}
	var resp struct{
		Status int `json:"status"`
		Seed string `json:"seed"`
	}
	err3 := json.Unmarshal(b, &resp)
	if err3 != nil {
		fmt.Printf(err3.Error())
		err = err3
		return
	}
	return resp.Status,resp.Seed,nil
}

func getSignature(challenge string, check string)(status int, signature string, err error)  {
	data := url.Values{}
	data.Set("challenge", challenge)
	data.Set("check", check)
	data.Set("api_pwd", api_pwd)
	b, err1 := client_Post(app_url+rsun_path+"s", data.Encode())
	if err1 != nil {
		fmt.Printf(err1.Error())
		err = err1
		return
	}
	var resp struct{
		Status int `json:"status"`
		Signature string `json:"signature"`
	}
	err2 := json.Unmarshal(b, &resp)
	if err2 != nil {
		fmt.Printf(err2.Error())
		err = err2
		return
	}
	return resp.Status,resp.Signature,nil
}

func getVerify(signature string, challenge string, token string, passwd string, check string)(verify bool, err error)  {
	data := url.Values{}
	data.Set("sign", signature)
	data.Set("challenge", challenge)
	data.Set("token", token)
	data.Set("passwd", passwd)
	data.Set("check", check)
	data.Set("api_pwd", api_pwd)
	b, err1 := client_Post(app_url+rsun_path+"v", data.Encode())
	if err1 != nil{
		fmt.Printf(err1.Error())
		err = err1
		return
	}
	var resp struct{
		Verify bool `json:"verify"`
	}
	err2 := json.Unmarshal(b, &resp)
	if err2 != nil {
		fmt.Printf(err2.Error())
		err = err2
		return
	}
	return resp.Verify,nil
}

func getEncrypt(plain string, token string, passwd string, check string)(status int, cipher string, err error)  {
	data:=url.Values{}
	data.Set("plain",plain)
	data.Set("token",token)
	data.Set("passwd",passwd)
	data.Set("check",check)
	data.Set("api_pwd", api_pwd)
	b, err1 := client_Post(app_url+rsun_path+"e",data.Encode())
	if err1!=nil {
		err = err1
		fmt.Printf(err1.Error())
		return
	}
	var resp struct{
		Cipher string `json:"cipher"`
		Status int `json:"status"`
	}
	err2 := json.Unmarshal(b, &resp)
	if err2!=nil{
		err = err2
		fmt.Printf(err2.Error())
		return
	}
	return resp.Status,resp.Cipher,nil
}

func getDecrypt(cipher string)(status int, plain string, err error)  {
	data:=url.Values{}
	data.Set("cipher",cipher)
	data.Set("api_pwd", api_pwd)
	b, err1 := client_Post(app_url+rsun_path+"d",data.Encode())
	if err1!=nil {
		err = err1
		fmt.Printf(err1.Error())
		return
	}
	var resp struct{
		Plain string `json:"plain"`
		Status int `json:"status"`
	}
	err2 := json.Unmarshal(b, &resp)
	if err2!=nil{
		err = err2
		fmt.Printf(err2.Error())
		return
	}
	return resp.Status,resp.Plain,nil
}

func client_Post(url string, data string) (body []byte, err error) {
	client := &http.Client{}
	req, err := http.NewRequest("POST", url, strings.NewReader(data))
	req.Close = true

	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Add("Content-Length", strconv.Itoa(len(data)))
	resp, err1 := client.Do(req)
	if err1 != nil {
		err = err1
		return
	}
	defer resp.Body.Close()

	b, err2 := ioutil.ReadAll(resp.Body)
	if err2 != nil {
		err = err2
		return
	}
	body = b
	return
}

func client_Get(url string) (body []byte, err error) {
	client := &http.Client{}
	req, err := http.NewRequest("GET", url, nil)
	req.Close = true
	resp, err1 := client.Do(req)
	if err1 != nil {
		err = err1
		return
	}
	defer resp.Body.Close()

	b, err2 := ioutil.ReadAll(resp.Body)
	if err2 != nil {
		err = err2
		return
	}
	body = b
	return
}
