package com.example.noreact.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noreact.R;
import com.example.noreact.model.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AIViewHolder) {
            ((AIViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeMessage(ChatMessage message) {
        int index = messages.indexOf(message);
        if (index != -1) {
            messages.remove(index);
            notifyItemRemoved(index);
        }
    }

    // User Message ViewHolder
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        TextView textTimestamp;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
        }

        void bind(ChatMessage message) {
            textMessage.setText(message.getMessage());
            // Uncomment to show timestamp
            // textTimestamp.setText(timeFormat.format(new Date(message.getTimestamp())));
            // textTimestamp.setVisibility(View.VISIBLE);
        }
    }

    // AI Message ViewHolder
    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        TextView textTimestamp;
        LinearLayout layoutLoading;
        ProgressBar progressTyping;

        AIViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
            layoutLoading = itemView.findViewById(R.id.layout_loading);
            progressTyping = itemView.findViewById(R.id.progress_typing);
        }

        void bind(ChatMessage message) {
            if (message.isLoading()) {
                // Show loading animation
                textMessage.setVisibility(View.GONE);
                layoutLoading.setVisibility(View.VISIBLE);
            } else {
                // Show actual message
                textMessage.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);
                textMessage.setText(message.getMessage());

                // Uncomment to show timestamp
                // textTimestamp.setText(timeFormat.format(new Date(message.getTimestamp())));
                // textTimestamp.setVisibility(View.VISIBLE);
            }
        }
    }
}