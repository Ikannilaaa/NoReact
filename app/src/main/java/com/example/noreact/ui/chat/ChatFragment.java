package com.example.noreact.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noreact.R;
import com.example.noreact.adapter.ChatAdapter;
import com.example.noreact.api.GeminiApiClient;
import com.example.noreact.api.GeminiApiService;
import com.example.noreact.model.ChatMessage;
import com.example.noreact.model.GeminiRequest;
import com.example.noreact.model.GeminiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFragment extends Fragment {
    private static final String GEMINI_API_KEY = "AIzaSyB4egABJpjJ-yhZXZg5Tu7NVRPeTOS7fTE";
    private RecyclerView recyclerChat;
    private EditText editMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private GeminiApiService apiService;
    private ChatMessage loadingMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        initViews(root);
        setupRecyclerView();
        setupApiService();
        setupClickListeners();

        return root;
    }

    private void initViews(View root) {
        recyclerChat = root.findViewById(R.id.recycler_chat);
        editMessage = root.findViewById(R.id.edit_message);
        btnSend = root.findViewById(R.id.btn_send);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);
    }

    private void setupApiService() {
        apiService = GeminiApiClient.getClient().create(GeminiApiService.class);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        // Send on Enter key press
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add user message
        ChatMessage userMessage = new ChatMessage(messageText, true);
        chatAdapter.addMessage(userMessage);
        scrollToBottom();

        // Clear input
        editMessage.setText("");

        // Show loading message
        showLoadingMessage();

        // Disable send button while processing
        btnSend.setEnabled(false);
        btnSend.setText("Sending...");

        // Send to Gemini API
        sendToGemini(messageText);
    }

    private void showLoadingMessage() {
        loadingMessage = new ChatMessage("AI sedang mengetik...", false, true);
        chatAdapter.addMessage(loadingMessage);
        scrollToBottom();
    }

    private void hideLoadingMessage() {
        if (loadingMessage != null) {
            chatAdapter.removeMessage(loadingMessage);
            loadingMessage = null;
        }
    }

    private void sendToGemini(String message) {
        // Create request
        GeminiRequest.Part part = new GeminiRequest.Part(message);
        GeminiRequest.Content content = new GeminiRequest.Content(Arrays.asList(part));
        GeminiRequest request = new GeminiRequest(Arrays.asList(content));

        // Make API call
        Call<GeminiResponse> call = apiService.generateContent(GEMINI_API_KEY, request);
        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                // Re-enable send button
                btnSend.setEnabled(true);
                btnSend.setText("➤");

                // Hide loading message
                hideLoadingMessage();

                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse geminiResponse = response.body();
                    if (geminiResponse.getCandidates() != null &&
                            !geminiResponse.getCandidates().isEmpty() &&
                            geminiResponse.getCandidates().get(0).getContent() != null &&
                            geminiResponse.getCandidates().get(0).getContent().getParts() != null &&
                            !geminiResponse.getCandidates().get(0).getContent().getParts().isEmpty()) {

                        String aiResponse = geminiResponse.getCandidates().get(0)
                                .getContent().getParts().get(0).getText();

                        ChatMessage aiMessage = new ChatMessage(aiResponse, false);
                        chatAdapter.addMessage(aiMessage);
                        scrollToBottom();
                    } else {
                        showError("Tidak ada respons dari AI");
                    }
                } else {
                    showError("Gagal mendapatkan respons: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                // Re-enable send button
                btnSend.setEnabled(true);
                btnSend.setText("SEND");

                // Hide loading message
                hideLoadingMessage();

                showError("Kesalahan jaringan: " + t.getMessage());
            }
        });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            recyclerChat.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }
}