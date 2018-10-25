<?php
/**
 * Created by PhpStorm.
 * User: willyy
 * Date: 2018/10/25
 * Time: 20:32
 */

class rsunx
{
    private $app_port = "2080";
    private $app_url = "http://127.0.0.1:2080";
    private $rsun_path = "/hades?cmd=";
    private $api_pwd = "38148a57d6c7f32c";
    function __construct($url = "http://127.0.0.1:2080", $path = "/hades?cmd=")
    {
        $this->app_url = $url;
        $this->rsun_path = $path;
    }
    function getRand(){
        $rand = $this->client_get("c");
        return $rand['rand'];
    }
    function getParam(){
        return $this->client_get("p");
    }
    function getKeySeed($token, $passwd, $element){
        $post_data = array("element"=>$element,
                        "token"=>$token,
                        "passwd"=>$passwd);
        return $this->client_post("r",$post_data);
    }
    function getSignature($challenge, $check){
        $post_data = array("check"=>$check,
                        "challenge"=>$challenge);
        return $this->client_post("s",$post_data);
    }
    function getVerify($signature, $challenge, $token, $passwd, $check){
        $post_data = array("signature"=>$signature,
                        "token"=>$token,
                        "passwd"=>$passwd,
                        "check"=>$check,
                        "challenge"=>$challenge);
        return $this->client_post("v",$post_data);
    }
    function getEncrypt($plain, $token, $passwd, $check){
        $post_data = array("plain"=>$plain,
                        "token"=>$token,
                        "passwd"=>$passwd,
                        "check"=>$check);
        return $this->client_post("e",$post_data);
    }
    function getDecrypt($cipher){
        $post_data = array("cipher"=>$cipher);
        return $this->client_post("d",$post_data);
    }
    private function client_post($cmd, $post_data){
        $post_data['api_pwd'] = "38148a57d6c7f32c";
        $post_body = $this->encode_array($post_data);
        $curl = curl_init();
        curl_setopt_array($curl, array(
            CURLOPT_PORT => $this->app_port,
            CURLOPT_URL => $this->app_url.$this->rsun_path.$cmd,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => "",
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 30,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => "POST",
            CURLOPT_POSTFIELDS => $post_body,
            CURLOPT_HTTPHEADER => array(
                "Content-Type: application/x-www-form-urlencoded",
            ),
        ));
        $response = curl_exec($curl);
        $err = curl_error($curl);
        curl_close($curl);
        if ($err) {
            return "cURL Error #:" . $err;
        } else {
            return json_decode($response,true);
        }
    }
    private function client_get($cmd){
        $curl = curl_init();
        curl_setopt_array($curl, array(
            CURLOPT_PORT => $this->app_port,
            CURLOPT_URL => $this->app_url.$this->rsun_path.$cmd,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => "",
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 30,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => "GET",
            CURLOPT_HTTPHEADER => array(
                "Content-Type: application/x-www-form-urlencoded"
            ),
        ));
        $response = curl_exec($curl);
        $err = curl_error($curl);
        curl_close($curl);
        if ($err) {
            return "cURL Error #:" . $err;
        } else {
            return json_decode($response,true);
        }
    }
    private function encode_array($args)
    {
        if(!is_array($args)) return false;
        $c = 0;
        $out = '';
        foreach($args as $name => $value)
        {
            if($c++ != 0) $out .= '&';
            $out .= urlencode("$name").'=';
            if(is_array($value))
            {
                $out .= urlencode(serialize($value));
            }else{
                $out .= urlencode("$value");
            }
        }
        return $out;
    }
}

