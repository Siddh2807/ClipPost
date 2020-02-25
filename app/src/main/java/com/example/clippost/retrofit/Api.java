package com.example.clippost.retrofit;

import com.example.clippost.Clip;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api
{
  @FormUrlEncoded
  @POST("base_upload_clip")
  Call<List<Clip>> uploadImage(@Field("name") String title, @Field("baseimage") String image);
}