package com.example.noreact.ui.history;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.noreact.R;
import com.example.noreact.model.HistoryItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    private ImageView ivFoodImage;
    private TextView tvFoodName, tvScanDate, tvConfidence, tvAllergenInfo, tvStatus;
    private CardView cardAllergens;

    private FirebaseFirestore firestore;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Riwayat");
        }

        initViews();
        initFirebase();
        loadHistoryDetail();
    }

    private void initViews() {
        ivFoodImage = findViewById(R.id.ivFoodImage);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvScanDate = findViewById(R.id.tvScanDate);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvAllergenInfo = findViewById(R.id.tvAllergenInfo);
        tvStatus = findViewById(R.id.tvStatus);
        cardAllergens = findViewById(R.id.cardAllergens);
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", new Locale("id", "ID"));
    }

    private void loadHistoryDetail() {
        String historyId = getIntent().getStringExtra("history_id");
        if (historyId == null) {
            Toast.makeText(this, "ID riwayat tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore.collection("history")
                .document(historyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HistoryItem historyItem = documentSnapshot.toObject(HistoryItem.class);
                        if (historyItem != null) {
                            displayHistoryDetail(historyItem);
                        }
                    } else {
                        Toast.makeText(this, "Data riwayat tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat detail: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayHistoryDetail(HistoryItem historyItem) {
        // Set food name
        tvFoodName.setText(formatFoodName(historyItem.getFoodName()));

        // Set scan date
        if (historyItem.getScanDate() != null) {
            tvScanDate.setText(dateFormat.format(historyItem.getScanDate()));
        }

        // Set confidence
        tvConfidence.setText(String.format("Tingkat Kepercayaan: %.1f%%",
                historyItem.getConfidence() * 100));

        // Set image
        if (historyItem.getImageBase64() != null && !historyItem.getImageBase64().isEmpty()) {
            Bitmap bitmap = base64ToBitmap(historyItem.getImageBase64());
            if (bitmap != null) {
                ivFoodImage.setImageBitmap(bitmap);
            } else {
                ivFoodImage.setImageResource(R.drawable.placeholder_food);
            }
        } else {
            ivFoodImage.setImageResource(R.drawable.placeholder_food);
        }

        // Set allergen information
        displayAllergenInfo(historyItem.getAllergens(), historyItem.getStatus());

        // Set status
        displayStatusInfo(historyItem.getStatus());
    }

    private void displayAllergenInfo(List<String> allergens, String status) {
        if ("success".equals(status) && allergens != null && !allergens.isEmpty()) {
            cardAllergens.setVisibility(android.view.View.VISIBLE);
            StringBuilder allergenText = new StringBuilder();

            for (String allergen : allergens) {
                allergenText.append("• ").append(getAllergenDisplayName(allergen)).append("\n");
            }

            allergenText.append("\n💡 Harap periksa bahan-bahan makanan jika Anda memiliki alergi terhadap zat-zat di atas.");
            tvAllergenInfo.setText(allergenText.toString());
        } else if ("success".equals(status)) {
            cardAllergens.setVisibility(android.view.View.VISIBLE);
            tvAllergenInfo.setText("✅ Tidak ada alergen yang diketahui untuk makanan ini.\n\n💡 Namun, tetap berhati-hati jika Anda memiliki alergi khusus.");
        } else {
            cardAllergens.setVisibility(android.view.View.GONE);
        }
    }

    private void displayStatusInfo(String status) {
        switch (status) {
            case "success":
                tvStatus.setText("✅ Scan berhasil dilakukan\n\nMakanan berhasil diidentifikasi dengan baik.");
                break;
            case "no_food":
                tvStatus.setText("❌ Bukan makanan terdeteksi\n\nAplikasi ini dirancang khusus untuk mendeteksi makanan. Silakan coba dengan foto makanan.");
                break;
            case "error":
                tvStatus.setText("⚠️ Terjadi kesalahan\n\nTerjadi kesalahan saat memproses gambar. Silakan coba lagi.");
                break;
            default:
                tvStatus.setText("❓ Status tidak diketahui");
                break;
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

    private String getAllergenDisplayName(String allergen) {
        switch (allergen.toLowerCase()) {
            case "telur": return "Telur";
            case "gluten": return "Gluten (Gandum)";
            case "cabai": return "Cabai/Pedas";
            case "santan": return "Santan (Kelapa)";
            case "seafood": return "Seafood (Makanan Laut)";
            case "kacang": return "Kacang-kacangan";
            case "kedelai": return "Kedelai";
            case "kecambah": return "Kecambah";
            case "kerang": return "Kerang";
            default: return allergen;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}