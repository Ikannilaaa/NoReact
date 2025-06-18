// ChatAPIService.java
package com.example.noreact.api;

import com.example.noreact.model.ChatAPIModels;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ChatAPIService {

    @POST("v1/chat/completions")
    Call<ChatAPIModels.ChatResponse> getChatCompletion(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Body ChatAPIModels.ChatRequest request
    );
}