package com.christianas.tiplearning.ApiService;



import com.christianas.tiplearning.Model.Api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @POST("/api/v1/Api.php?apicall=sendTipLearningEmail")
    @FormUrlEncoded
    Call<Api> sendEmail(@Field("email") String email,
    @Field("courseTitle") String courseTitle);

}
