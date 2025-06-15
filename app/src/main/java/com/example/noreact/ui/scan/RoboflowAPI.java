// RoboflowAPI.java
package com.example.noreact.ui.scan;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RoboflowAPI {

    @Multipart
    @POST("indonesianfoodallergendetector-zd40l/1")
    Call<RoboflowResponse> detectFood(
            @Part MultipartBody.Part image,
            @Query("api_key") String apiKey
    );

    static RoboflowAPI create() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://serverless.roboflow.com/") // Base URL for Roboflow
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RoboflowAPI.class);
    }
}