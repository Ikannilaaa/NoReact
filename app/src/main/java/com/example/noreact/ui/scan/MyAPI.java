package com.example.noreact.ui.scan;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MyAPI {

    @Multipart
    @POST("Api.php?apicall=upload")
    Call<UploadResponse> uploadImage(
            @Part MultipartBody.Part image,
            @Part("desc") RequestBody desc
    );

    static MyAPI create() {
        return new Retrofit.Builder()
                .baseUrl("http://192.168.189.198/ImageUploader/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyAPI.class);
    }
}
