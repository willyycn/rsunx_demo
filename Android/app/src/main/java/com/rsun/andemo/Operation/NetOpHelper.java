package com.rsun.andemo.Operation;

import android.content.Context;

import com.rsun.andemo.inf.RSunApi;
import com.rsun.kit.RSunKit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NetOpHelper {
    private static final String TAG = "NetOpHelper";
    public static final String ServerUrl = "http://10.10.252.13:8000";
    public static final String ServerParam = "412a9e72f2120b69d685200d834d4a3e5349c09ba00583f762929af5c6379c6dcdab5ad96b5ee53e5ad0ac912f57a347da3ff4fb474847e534040086c0830b2d";
    public static final String ServerToken = "{\"neo_server\":\"willyy@R-Sun-X\"}";
    public static final String ServerChkSum = "5adad94944b686f8b47d237681d4f0ce5fc89d4d14b1f4a8d38866ef37031e1c654d9826bc67b931d0836d8676957d959e40560054c3a6e4601754698f7c99ca";
    private static NetOpHelper mHelper = null;
    private Retrofit retrofit;
    private Context mContext;

    private NetOpHelper(Context context) {
        mContext = context;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(ServerUrl)
                .client(okHttpClient)
                .build();
    }

    public static NetOpHelper getHelper(Context context) {
        if (mHelper == null) {
            mHelper = new NetOpHelper(context);
        }
        return mHelper;
    }

    public interface NetOpCallback{
        void onResponse(int status, Map<String,String> response);
        void onFailure(Error error);
    }

    public void generate(final String token, final String passwd, final String challenge, final NetOpCallback netOpCallback){
        final Map<String,String> dic1 = RSunKit.rsunxInit(token, passwd);
        final String secret = dic1.get("secret");
        String element = dic1.get("element");
        final Map<String,String> dic2 = RSunKit.rsunxEncrypt(token,ServerToken,"",ServerChkSum,ServerParam);
        Call<ResponseBody> generateUserQueue = retrofit.create(RSunApi.class).generate(dic2.get("cipher"),challenge,passwd,element);
        generateUserQueue.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String r = response.body().string();
                    JSONObject jsonObject = new JSONObject(r);
                    int status = jsonObject.getInt("status");
                    if (status==1){
                        String sign = jsonObject.getString("signature");
                        int v = RSunKit.rsunxVerify(ServerToken,"",challenge,sign,ServerChkSum,ServerParam);
                        if (v==1){
                            String seed = jsonObject.getString("seed");
                            final Map<String,String> dic3 = RSunKit.rsunxGenerate(token,passwd,seed,secret);
                            Map<String,String> map = new HashMap<String,String>();
                            map.put("prikey",dic3.get("prikey"));
                            map.put("tk_sum",dic3.get("t_chk_sum"));
                            map.put("challenge",jsonObject.getString("rand"));
                            netOpCallback.onResponse(1,map);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                netOpCallback.onFailure(new Error("network failed!"));
            }
        });
    }
    public void verifySign(final String signature, final String token, final String passwd, final String tk_sum, final NetOpCallback netOpCallback){
        Call<ResponseBody> verifyQueue = retrofit.create(RSunApi.class).verifySign(token,signature,passwd,tk_sum);
        verifyQueue.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String r = null;
                try {
                    r = response.body().string();
                    JSONObject jsonObject = new JSONObject(r);
                    Map<String,String> map = new HashMap<String,String>();
                    map.put("verify",jsonObject.getString("verify"));
                    netOpCallback.onResponse(1, map);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
