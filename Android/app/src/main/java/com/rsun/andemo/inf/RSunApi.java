package com.rsun.andemo.inf;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RSunApi {
    @POST("/demo/verify")
    @FormUrlEncoded
    Call<ResponseBody> verifySign(@Field("token") String token,@Field("sign") String sign,@Field("passwd") String passwd, @Field("check") String check);

    @POST("/demo/generate")
    @FormUrlEncoded
    Call<ResponseBody> generate(@Field("token") String token, @Field("challenge") String challenge, @Field("passwd") String passwd,@Field("element") String element);
}
