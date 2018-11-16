package com.example.admin.somedemo.advertisedemo;

import com.example.admin.somedemo.advertisedemo.model.AdvertiseData;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Author liang
 * Date
 * Dsc:
 */
public interface ApiService {


    //请求服务器获取广告信息
    @GET("/api/advertising")
    Call<AdvertiseData> getAdvertiseData(@Query("big_screen_number") String screen_nuber);

    //下载视频
    @Streaming
    @GET("/{videoMd5}")
    Call<ResponseBody> getAdvertiseSources(@Path("videoMd5") String videMd5);
}
