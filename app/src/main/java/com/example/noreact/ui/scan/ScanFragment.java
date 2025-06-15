package com.example.noreact.ui.scan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.noreact.databinding.FragmentScanBinding;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private Uri selectedImageUri;
    private File capturedImageFile;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private AllergenAdapter allergenAdapter;

    // API Key Anda
    private static final String API_KEY = "wVtzCReuYMeUQAhnnM83";
    private static final int MAX_IMAGE_DIMENSION = 1024; // Batas resolusi gambar

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        new ViewModelProvider(this).get(ScanViewModel.class);

        setupActivityResultLaunchers();
        setupButtonListeners();
        setupRecyclerView();

        return root;
    }

    private void setupActivityResultLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(getContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null && result.getData().getData() != null) {
                        selectedImageUri = result.getData().getData();
                        loadImageFromUri(selectedImageUri);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (capturedImageFile != null && capturedImageFile.exists()) {
                            selectedImageUri = Uri.fromFile(capturedImageFile);
                            loadImageFromUri(selectedImageUri);
                        }
                    }
                });
    }

    private void setupButtonListeners() {
        binding.galleryButton.setOnClickListener(v -> openGallery());
        binding.cameraButton.setOnClickListener(v -> checkCameraPermission());
        binding.detectAllergenButton.setOnClickListener(v -> detectAllergen());
    }

    private void setupRecyclerView() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        binding.allergensRecyclerView.setLayoutManager(layoutManager);
    }

    private void loadImageFromUri(Uri uri) {
        try {
            Bitmap bitmap = getScaledBitmapFromUri(uri);
            binding.imageView.setImageBitmap(bitmap);
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.detectAllergenButton.setEnabled(true);
            hideResults();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.showSnackbar(binding.getRoot(), "Gagal memuat gambar");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void checkCameraPermission() {
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            capturedImageFile = new File(requireContext().getCacheDir(), "camera_captured_" + System.currentTimeMillis() + ".jpg");
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    capturedImageFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showSnackbar(binding.getRoot(), "Gagal membuka kamera.");
        }
    }

// Key fixes untuk ScanFragment.java

    // 1. Update detectAllergen method
    private void detectAllergen() {
        if (selectedImageUri == null) {
            Utils.showSnackbar(binding.imageView, "Pilih gambar terlebih dahulu.");
            return;
        }

        showLoading(true);

        try {
            // Convert URI to File dengan error handling yang lebih baik
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
            if (inputStream == null) {
                showLoading(false);
                Utils.showSnackbar(binding.imageView, "Gagal membaca gambar.");
                return;
            }

            File file = new File(requireContext().getCacheDir(),
                    "temp_image_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // Compress image jika terlalu besar
            if (file.length() > 4 * 1024 * 1024) { // 4MB
                compressImage(file);
            }

            // Create request body
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            // Make API call
            RoboflowAPI api = RoboflowAPI.create();
            Call<RoboflowResponse> call = api.detectFood(body, API_KEY);

            call.enqueue(new Callback<RoboflowResponse>() {
                @Override
                public void onResponse(Call<RoboflowResponse> call, Response<RoboflowResponse> response) {
                    showLoading(false);

                    // Log response untuk debugging
                    android.util.Log.d("ScanFragment", "Response code: " + response.code());
                    if (response.body() != null) {
                        android.util.Log.d("ScanFragment", "Predictions count: " +
                                (response.body().getPredictions() != null ? response.body().getPredictions().size() : "null"));
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        handleDetectionResult(response.body());
                    } else {
                        String errorMsg = "Gagal mendeteksi makanan. Code: " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += " - " + response.errorBody().string();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        Utils.showSnackbar(binding.imageView, errorMsg);
                    }

                    // Clean up temp file
                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onFailure(Call<RoboflowResponse> call, Throwable t) {
                    showLoading(false);
                    android.util.Log.e("ScanFragment", "API call failed", t);
                    Utils.showSnackbar(binding.imageView, "Error: " + t.getMessage());

                    // Clean up temp file
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });

        } catch (Exception e) {
            showLoading(false);
            android.util.Log.e("ScanFragment", "Exception in detectAllergen", e);
            Utils.showSnackbar(binding.imageView, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    // 2. Tambahkan method untuk compress image
    private void compressImage(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();
        } catch (Exception e) {
            android.util.Log.e("ScanFragment", "Failed to compress image", e);
        }
    }

    // 3. Update handleDetectionResult dengan threshold confidence
    private void handleDetectionResult(RoboflowResponse response) {
        if (response.getPredictions() != null && !response.getPredictions().isEmpty()) {
            // Filter predictions dengan confidence > 0.3 (30%)
            RoboflowResponse.Prediction bestPrediction = null;
            double highestConfidence = 0.3; // minimum threshold

            for (RoboflowResponse.Prediction prediction : response.getPredictions()) {
                if (prediction.getConfidence() > highestConfidence) {
                    bestPrediction = prediction;
                    highestConfidence = prediction.getConfidence();
                }
            }

            if (bestPrediction != null) {
                String foodName = bestPrediction.getClassName();
                double confidence = bestPrediction.getConfidence();

                // Format food name for display
                String displayName = AllergenData.formatFoodName(foodName);

                // Get allergens for this food
                List<String> allergens = AllergenData.getAllergens(foodName);

                // Display results
                showDetectionResults(displayName, confidence, allergens);
            } else {
                Utils.showSnackbar(binding.imageView, "Confidence terlalu rendah. Coba dengan gambar yang lebih jelas.");
            }
        } else {
            Utils.showSnackbar(binding.imageView, "Tidak dapat mendeteksi makanan. Coba dengan gambar lain.");
        }
    }

    private Bitmap getScaledBitmapFromUri(Uri uri) throws IOException {
        InputStream input = requireContext().getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        if (input != null) input.close();

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        int scale = 1;

        while (originalWidth / scale / 2 >= MAX_IMAGE_DIMENSION || originalHeight / scale / 2 >= MAX_IMAGE_DIMENSION) {
            scale *= 2;
        }

        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inSampleSize = scale;
        InputStream scaleInput = requireContext().getContentResolver().openInputStream(uri);
        Bitmap scaledBitmap = BitmapFactory.decodeStream(scaleInput, null, scaleOptions);
        if (scaleInput != null) scaleInput.close();

        return scaledBitmap;
    }

    private void showDetectionResults(String foodName, double confidence, List<String> allergens) {
        binding.foodNameText.setText(foodName);

        DecimalFormat df = new DecimalFormat("#.#");
        binding.confidenceText.setText(String.format("%s%%", df.format(confidence * 100)));

        if (allergens != null && !allergens.isEmpty()) {
            allergenAdapter = new AllergenAdapter(allergens);
            binding.allergensRecyclerView.setAdapter(allergenAdapter);
            binding.allergensRecyclerView.setVisibility(View.VISIBLE);
            binding.noAllergensText.setVisibility(View.GONE);
        } else {
            binding.allergensRecyclerView.setVisibility(View.GONE);
            binding.noAllergensText.setVisibility(View.VISIBLE);
        }
        binding.resultsCard.setVisibility(View.VISIBLE);
    }

    private void hideResults() {
        binding.resultsCard.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.detectAllergenButton.setEnabled(!show);
        binding.galleryButton.setEnabled(!show);
        binding.cameraButton.setEnabled(!show);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}