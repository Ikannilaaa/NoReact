// ChatViewModel.java
package com.example.noreact.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.noreact.api.APIClient;
import com.example.noreact.api.ChatAPIService;
import com.example.noreact.model.ChatAPIModels;
import com.example.noreact.model.ChatMessage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private static final String API_KEY = "8c293618f19d4e2d8b1e9b8c801c8a18";
    private static final String MODEL_NAME = "gpt-4o";

    private ChatAPIService chatAPIService;
    private MutableLiveData<List<ChatMessage>> chatMessages;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public ChatViewModel() {
        chatAPIService = APIClient.getChatAPIService();
        chatMessages = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();

        // Add welcome message
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("Halo! Saya adalah asisten AI Anda. Ada yang bisa saya bantu?", false));
        chatMessages.setValue(messages);
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void sendMessage(String message) {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages == null) currentMessages = new ArrayList<>();

        // Add user message
        currentMessages.add(new ChatMessage(message, true));
        chatMessages.setValue(currentMessages);

        // Set loading state
        isLoading.setValue(true);

        // Prepare API request
        sendToAPI(message, currentMessages);
    }

    private void sendToAPI(String userMessage, List<ChatMessage> currentMessages) {
        // Prepare messages for API
        List<ChatAPIModels.Message> apiMessages = new ArrayList<>();

        // Add system message
        apiMessages.add(new ChatAPIModels.Message("system",
                "Anda adalah asisten AI yang membantu dan ramah. Jawab dalam bahasa Indonesia."));

        // Add conversation history (last 10 messages for context)
        int startIndex = Math.max(0, currentMessages.size() - 10);
        for (int i = startIndex; i < currentMessages.size(); i++) {
            ChatMessage msg = currentMessages.get(i);
            if (!msg.getMessage().startsWith("Halo! Saya adalah")) {
                String role = msg.isUser() ? "user" : "assistant";
                apiMessages.add(new ChatAPIModels.Message(role, msg.getMessage()));
            }
        }

        // Create request
        ChatAPIModels.ChatRequest request = new ChatAPIModels.ChatRequest(
                MODEL_NAME,
                apiMessages,
                1000, // max tokens
                0.7   // temperature
        );

        // Make API call
        Call<ChatAPIModels.ChatResponse> call = chatAPIService.getChatCompletion(
                "Bearer " + API_KEY,
                "application/json",
                request
        );

        call.enqueue(new Callback<ChatAPIModels.ChatResponse>() {
            @Override
            public void onResponse(Call<ChatAPIModels.ChatResponse> call, Response<ChatAPIModels.ChatResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    ChatAPIModels.ChatResponse chatResponse = response.body();

                    if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                        String aiResponse = chatResponse.getChoices().get(0).getMessage().getContent();

                        // Add AI response
                        List<ChatMessage> updatedMessages = chatMessages.getValue();
                        if (updatedMessages != null) {
                            updatedMessages.add(new ChatMessage(aiResponse, false));
                            chatMessages.setValue(updatedMessages);
                        }
                    } else {
                        errorMessage.setValue("Tidak ada respons dari AI");
                    }
                } else {
                    errorMessage.setValue("Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ChatAPIModels.ChatResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}