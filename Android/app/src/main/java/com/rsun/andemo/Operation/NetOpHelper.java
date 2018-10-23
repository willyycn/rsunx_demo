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
    public static final String ServerUrl = "http://10.10.252.13:3000";
    public static final String ServerParam = "fca9952d08b178ffcf8ee03750e425f63a68e32d24f38e26e65f59dc8a056460545ed89a8c175dda576d62dc1d6ba3e092b5a34495525de1c71961ec11b7dbea";
    public static final String ServerToken = "{\"neo_server\":\"willyy@R-Sun-X\"}";
    public static final String ServerChkSum = "a2c51e7056bc315df8ce67d8330564803598ac10909c53f10f33fd3c7aba46603add8883c4ea88b5bb6c778f00e47836470918b720e92a068db0005a6c0df643";
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
