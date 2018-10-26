package rsunx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RSunX {
    private static String app_url = "http://127.0.0.1:2080";
    private static String rsun_path = "/hades?cmd=";
    private static String api_pwd = "38148a57d6c7f32c";
    public static JsonNode  getRand() throws IOException {
        return client_get("c");
    }
    public static JsonNode getParam() throws IOException {
        return client_get("p");
    }
    public static JsonNode getKeySeed(String token, String passwd, String element) throws IOException {
        HashMap<String,String>postData = new HashMap<>();
        postData.put("token",token);
        postData.put("passwd",passwd);
        postData.put("element",element);
        return client_post("r",postData);
    }
    public static JsonNode getSignature(String challenge, String check) throws IOException {
        HashMap<String,String>postData = new HashMap<>();
        postData.put("challenge",challenge);
        postData.put("check",check);
        return client_post("s",postData);
    }
    public static JsonNode getVerify(String signature, String challenge, String token, String passwd, String check) throws IOException {
        HashMap<String,String>postData = new HashMap<>();
        postData.put("sign",signature);
        postData.put("token",token);
        postData.put("passwd",passwd);
        postData.put("challenge",challenge);
        postData.put("check",check);
        return client_post("v",postData);
    }
    public static JsonNode getEncrypt(String plain, String token, String passwd, String check) throws IOException {
        HashMap<String,String>postData = new HashMap<>();
        postData.put("plain",plain);
        postData.put("token",token);
        postData.put("passwd",passwd);
        postData.put("check",check);
        return client_post("e",postData);
    }
    public static JsonNode getDecrypt(String cipher) throws IOException {
        HashMap<String,String>postData = new HashMap<>();
        postData.put("cipher",cipher);
        return client_post("d",postData);
    }
    private static JsonNode client_get(String cmd) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(app_url+rsun_path+cmd)
                .get()
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        String r = response.body() != null ? response.body().string() : null;
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(r);
    }

    private static JsonNode client_post(String cmd, HashMap<String,String>map) throws IOException {
        map.put("api_pwd",api_pwd);
        String dataString = getDataString(map);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, dataString);
        Request request = new Request.Builder()
                .url(app_url+rsun_path+cmd)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        String r = response.body() != null ? response.body().string() : null;
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(r);
    }
    private static String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
