package com.muvit.passenger.AsyncTask;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RaveApiRequests {
    @FormUrlEncoded
    @POST("flwv3-pug/getpaidx/api/charge")
    Call<JsonElement> makeCharge(@Field("PBFPubKey") String PBFPubKey,
                                 @Field("client") String client,
                                 @Field("alg") String alg);

    @FormUrlEncoded
    @POST("flwv3-pug/getpaidx/api/validatecharge")
    Call<JsonElement> verifyCharge(@Field("PBFPubKey") String PBFPubKey,
                                   @Field("transaction_reference") String transactionReference,
                                   @Field("otp") String otp);
}
