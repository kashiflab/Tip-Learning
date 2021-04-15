package com.christianas.tiplearning.Utils;


import com.christianas.tiplearning.ApiService.APIService;
import com.christianas.tiplearning.Retrofit.RetrofitClient;

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "https://www.inventerit.com";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
