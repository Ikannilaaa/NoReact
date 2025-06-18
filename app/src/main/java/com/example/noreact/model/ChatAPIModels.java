// ChatAPIModels.java
package com.example.noreact.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Request Models
public class ChatAPIModels {

    public static class ChatRequest {
        @SerializedName("model")
        private String model;

        @SerializedName("messages")
        private List<Message> messages;

        @SerializedName("max_tokens")
        private int maxTokens;

        @SerializedName("temperature")
        private double temperature;

        public ChatRequest(String model, List<Message> messages, int maxTokens, double temperature) {
            this.model = model;
            this.messages = messages;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
        }

        // Getters and Setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }

        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
    }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Response Models
    public static class ChatResponse {
        @SerializedName("choices")
        private List<Choice> choices;

        @SerializedName("usage")
        private Usage usage;

        // Getters and Setters
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }

        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
    }

    public static class Choice {
        @SerializedName("message")
        private Message message;

        @SerializedName("finish_reason")
        private String finishReason;

        // Getters and Setters
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }

        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }

    public static class Usage {
        @SerializedName("prompt_tokens")
        private int promptTokens;

        @SerializedName("completion_tokens")
        private int completionTokens;

        @SerializedName("total_tokens")
        private int totalTokens;

        // Getters and Setters
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }
}