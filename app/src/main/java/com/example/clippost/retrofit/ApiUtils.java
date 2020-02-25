package com.example.clippost.retrofit;

public class ApiUtils {
    private static final String BASE_URL = "http://162.213.190.124:10544/readersum/api/";

    public ApiUtils() { }

    public static Api getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(Api.class);
    }
}