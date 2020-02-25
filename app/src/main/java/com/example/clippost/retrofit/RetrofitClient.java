package com.example.clippost.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitClient {
    private static Retrofit retrofit = null;

    /*Retrofit needs a base URL to build its instance,*/
    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {

            Gson gson = new GsonBuilder ()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getRequestHeader())
                    .addConverterFactory(GsonConverterFactory.create(gson))

                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient getRequestHeader() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(70, TimeUnit.SECONDS)
                .build();

        return okHttpClient;
    }
}