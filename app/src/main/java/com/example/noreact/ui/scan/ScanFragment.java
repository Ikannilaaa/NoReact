package com.example.noreact.ui.scan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.noreact.R;
import com.example.noreact.model.HistoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private ImageView imageView;
    private Button btnCamera, btnGallery, btnDetect;
    private ProgressBar progressBar;
    private TextView tvResult;
    private LinearLayout placeholderLayout;
    private CardView resultCard;
    private Bitmap selectedBitmap;

    // API Roboflow configuration
    private static final String API_URL = "https://detect.roboflow.com";
    private static final String API_KEY = "wVtzCReuYMeUQAhnnM83";
    private static final String MODEL_ID = "indonesianfoodallergendetector-zd40l/7";

    // Allergen data
    private Map<String, List<String>> allergenData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scan, container, false);

        initViews(root);
        initAllergenData();
        setupClickListeners();

        return root;
    }

    private void initViews(View root) {
        imageView = root.findViewById(R.id.imageView);
        btnCamera = root.findViewById(R.id.btnCamera);
        btnGallery = root.findViewById(R.id.btnGallery);
        btnDetect = root.findViewById(R.id.btnDetect);
        progressBar = root.findViewById(R.id.progressBar);
        tvResult = root.findViewById(R.id.tvResult);
        placeholderLayout = root.findViewById(R.id.placeholderLayout);
        resultCard = root.findViewById(R.id.resultCard);
    }

    private void initAllergenData() {
        allergenData = new HashMap<>();
        allergenData.put("ayam_geprek", Arrays.asList("telur", "gluten", "cabai"));
        allergenData.put("ayam_goreng", Arrays.asList("gluten"));
        allergenData.put("ayam_pop", Arrays.asList("santan"));
        allergenData.put("cumi_goreng", Arrays.asList("seafood", "gluten"));
        allergenData.put("daging_rendang", Arrays.asList("santan", "cabai"));
        allergenData.put("dendeng_batokok", Arrays.asList("cabai"));
        allergenData.put("gado_gado", Arrays.asList("kacang", "telur", "kedelai", "kecambah", "cabai"));
        allergenData.put("ayam_gulai", Arrays.asList("santan", "cabai"));
        allergenData.put("gulai_ikan", Arrays.asList("santan", "seafood", "cabai"));
        allergenData.put("gulai_tambusu", Arrays.asList("telur", "santan", "cabai"));
        allergenData.put("gulai_tunjang", Arrays.asList("santan", "cabai"));
        allergenData.put("ikan_goreng", Arrays.asList("seafood"));
        allergenData.put("kentang_goreng", Arrays.asList("gluten"));
        allergenData.put("lontong_kupang", Arrays.asList("kerang", "gluten", "cabai"));
        allergenData.put("lumpia", Arrays.asList("gluten", "telur", "kedelai", "cabai"));
        allergenData.put("mie_goreng", Arrays.asList("gluten", "telur", "kedelai", "cabai"));
        allergenData.put("nasi_goreng", Arrays.asList("telur", "kedelai", "cabai"));
        allergenData.put("pecel_sayur", Arrays.asList("kacang", "kecambah", "cabai"));
        allergenData.put("rawon", Arrays.asList("kedelai"));
        allergenData.put("sate", Arrays.asList("kacang", "kedelai", "cabai"));
        allergenData.put("soto", Arrays.asList("santan", "telur", "kecambah", "cabai"));
        allergenData.put("telur_balado", Arrays.asList("telur", "cabai"));
        allergenData.put("telur_dadar", Arrays.asList("telur"));
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        btnGallery.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        btnDetect.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                detectAllergen();
            } else {
                Toast.makeText(getContext(), getString(R.string.select_image_first), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                selectedBitmap = (Bitmap) extras.get("data");
                displaySelectedImage();
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri selectedImageUri = data.getData();
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(
                            getActivity().getContentResolver(), selectedImageUri);
                    displaySelectedImage();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void displaySelectedImage() {
        if (selectedBitmap != null) {
            imageView.setImageBitmap(selectedBitmap);
            placeholderLayout.setVisibility(View.GONE);
            btnDetect.setVisibility(View.VISIBLE);
            resultCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                openCamera();
            } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
                openGallery();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
        }
    }

    private void detectAllergen() {
        progressBar.setVisibility(View.VISIBLE);
        btnDetect.setEnabled(false);
        resultCard.setVisibility(View.VISIBLE);
        tvResult.setText(getString(R.string.analyzing));

        // Convert bitmap to base64
        String base64Image = bitmapToBase64(selectedBitmap);

        // Create OkHttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Create request body - using form data for Roboflow API
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                base64Image);

        // Create request with proper Roboflow endpoint
        String url = API_URL + "/" + MODEL_ID + "?api_key=" + API_KEY + "&confidence=0.3&overlap=0.3";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();

        // Execute request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnDetect.setEnabled(true);
                        tvResult.setText("Error: " + e.getMessage());
                        Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnDetect.setEnabled(true);

                        if (response.isSuccessful()) {
                            parseAndDisplayResult(responseBody);
                        } else {
                            tvResult.setText("Error: " + response.code() + " " + response.message());
                            Toast.makeText(getContext(), getString(R.string.detection_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void parseAndDisplayResult(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONArray predictions = response.getJSONArray("predictions");

            if (predictions.length() > 0) {
                // Get the highest confidence prediction
                JSONObject bestPrediction = null;
                double highestConfidence = 0;

                for (int i = 0; i < predictions.length(); i++) {
                    JSONObject prediction = predictions.getJSONObject(i);
                    double confidence = prediction.getDouble("confidence");

                    if (confidence > highestConfidence) {
                        highestConfidence = confidence;
                        bestPrediction = prediction;
                    }
                }

                if (bestPrediction != null && highestConfidence > 0.3) { // Minimum confidence threshold
                    String detectedClass = bestPrediction.getString("class");
                    double confidence = bestPrediction.getDouble("confidence");

                    // Check if detected class is non-food item
                    if (isNonFoodClass(detectedClass)) {
                        displayNonFoodResult(detectedClass, confidence);
                    } else {
                        displayAllergenResult(detectedClass, confidence);
                    }
                } else {
                    tvResult.setText(getString(R.string.food_not_identified));
                }
            } else {
                tvResult.setText(getString(R.string.no_food_detected));
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("Error parsing result: " + e.getMessage());
        }
    }

    /**
     * Check if the detected class is a non-food item (human/unknown or object)
     */
    private boolean isNonFoodClass(String className) {
        String lowerClassName = className.toLowerCase();

        // Check for human/person related classes
        if (lowerClassName.contains("unknown") ||
                lowerClassName.contains("human") ||
                lowerClassName.contains("person") ||
                lowerClassName.contains("people") ||
                lowerClassName.equals("manusia")) {
            return true;
        }

        // Check for general object classes
        if (lowerClassName.contains("object") ||
                lowerClassName.contains("thing") ||
                lowerClassName.contains("item") ||
                lowerClassName.equals("benda") ||
                lowerClassName.equals("barang")) {
            return true;
        }

        // You can add more non-food class patterns here as needed
        return false;
    }

    private void saveToHistory(String foodName, double confidence, List<String> allergens, String status) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String imageBase64 = selectedBitmap != null ? bitmapToBase64(selectedBitmap) : null;

        HistoryItem historyItem = new HistoryItem();
        historyItem.setUserId(userId);
        historyItem.setFoodName(foodName);
        historyItem.setImageBase64(imageBase64);
        historyItem.setAllergens(allergens);
        historyItem.setConfidence(confidence);
        historyItem.setStatus(status);
        historyItem.setScanDate(new Date());

        FirebaseFirestore.getInstance()
                .collection("history")
                .add(historyItem)
                .addOnSuccessListener(documentReference -> {
                    // History saved successfully
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Riwayat berhasil disimpan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Gagal menyimpan ke riwayat: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Modifikasi method displayAllergenResult
    private void displayAllergenResult(String foodName, double confidence) {
        StringBuilder result = new StringBuilder();
        result.append("🍽️ Makanan Terdeteksi: ").append(formatFoodName(foodName)).append("\n");
        result.append("📊 Tingkat Kepercayaan: ").append(String.format("%.1f%%", confidence * 100)).append("\n\n");

        List<String> allergens = allergenData.get(foodName.toLowerCase().replace(" ", "_"));

        if (allergens != null && !allergens.isEmpty()) {
            result.append("⚠️ PERINGATAN ALERGEN:\n");
            for (String allergen : allergens) {
                result.append("• ").append(getAllergenDisplayName(allergen)).append("\n");
            }
            result.append("\n💡 Harap periksa bahan-bahan makanan jika Anda memiliki alergi terhadap zat-zat di atas.");
        } else {
            result.append("✅ Tidak ada alergen yang diketahui untuk makanan ini.\n");
            result.append("💡 Namun, tetap berhati-hati jika Anda memiliki alergi khusus.");
        }

        tvResult.setText(result.toString());

        // Save to history
        saveToHistory(foodName, confidence, allergens, "success");
    }

    // Modifikasi method displayNonFoodResult
    private void displayNonFoodResult(String detectedClass, double confidence) {
        StringBuilder result = new StringBuilder();
        result.append("❌ BUKAN MAKANAN\n\n");
        result.append("🍽️ Aplikasi ini dirancang khusus untuk mendeteksi makanan dan alergennya.\n\n");
        result.append("📱 Silakan ambil foto makanan untuk mendapatkan informasi alergen yang akurat.\n\n");
        result.append("💡 Tips: Pastikan foto menampilkan makanan dengan jelas dan pencahayaan yang cukup.");

        tvResult.setText(result.toString());

        // Save to history
        saveToHistory("Bukan Makanan", confidence, null, "no_food");
    }

    private String formatFoodName(String foodName) {
        // Convert underscore to space and capitalize each word
        String[] words = foodName.replace("_", " ").split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (formatted.length() > 0) formatted.append(" ");
            if (word.length() > 0) {
                formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
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
}