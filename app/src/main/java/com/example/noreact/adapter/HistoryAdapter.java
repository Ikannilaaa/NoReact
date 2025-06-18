package com.example.noreact.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noreact.R;
import com.example.noreact.model.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryItem> historyList;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnItemClickListener {
        void onItemClick(HistoryItem historyItem);
    }

    public HistoryAdapter(Context context) {
        this.context = context;
        this.historyList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateHistory(List<HistoryItem> newHistoryList) {
        this.historyList.clear();
        this.historyList.addAll(newHistoryList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFoodImage;
        private TextView tvFoodName, tvScanDate, tvStatus, tvConfidence;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvScanDate = itemView.findViewById(R.id.tvScanDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(historyList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(HistoryItem item) {
            // Set food name
            tvFoodName.setText(formatFoodName(item.getFoodName()));

            // Set scan date
            if (item.getScanDate() != null) {
                tvScanDate.setText(dateFormat.format(item.getScanDate()));
            }

            // Set status
            String statusText = getStatusText(item.getStatus());
            tvStatus.setText(statusText);
            tvStatus.setBackgroundResource(getStatusBackground(item.getStatus()));

            // Set confidence
            tvConfidence.setText(String.format("Kepercayaan: %.1f%%", item.getConfidence() * 100));

            // Set image
            if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
                Bitmap bitmap = base64ToBitmap(item.getImageBase64());
                if (bitmap != null) {
                    ivFoodImage.setImageBitmap(bitmap);
                } else {
                    ivFoodImage.setImageResource(R.drawable.placeholder_food);
                }
            } else {
                ivFoodImage.setImageResource(R.drawable.placeholder_food);
            }
        }

        private String formatFoodName(String foodName) {
            if (foodName == null) return "Unknown";

            String[] words = foodName.replace("_", " ").split(" ");
            StringBuilder formatted = new StringBuilder();

            for (String word : words) {
                if (formatted.length() > 0) formatted.append(" ");
                if (word.length() > 0) {
                    formatted.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1).toLowerCase());
                }
            }

            return formatted.toString();
        }

        private String getStatusText(String status) {
            switch (status) {
                case "success": return "✅ Berhasil";
                case "no_food": return "❌ Bukan Makanan";
                case "error": return "⚠️ Error";
                default: return "❓ Unknown";
            }
        }

        private int getStatusBackground(String status) {
            switch (status) {
                case "success": return R.drawable.status_success_background;
                case "no_food": return R.drawable.status_warning_background;
                case "error": return R.drawable.status_error_background;
                default: return R.drawable.item_background;
            }
        }

        private Bitmap base64ToBitmap(String base64String) {
            try {
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}