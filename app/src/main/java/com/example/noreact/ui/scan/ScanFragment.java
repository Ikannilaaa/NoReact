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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanFragment extends Fragment implements UploadRequestBody.UploadCallback {

    private FragmentScanBinding binding;
    private Uri selectedImageUri;
    private File capturedImageFile;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        new ViewModelProvider(this).get(ScanViewModel.class);

        // Permission camera
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(getContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null && result.getData().getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageView.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.showSnackbar(binding.imageView, "Gagal memuat gambar");
                        }
                    }
                });

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (capturedImageFile != null && capturedImageFile.exists()) {
                            selectedImageUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    requireContext().getPackageName() + ".provider",
                                    capturedImageFile
                            );
                            binding.imageView.setImageURI(selectedImageUri);
                        }
                    }
                });

        // Button listeners
        binding.galleryButton.setOnClickListener(v -> openGallery());
        binding.cameraButton.setOnClickListener(v -> checkCameraPermission());
        binding.detectAllergenButton.setOnClickListener(v -> uploadImage());

        return root;
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
            capturedImageFile = new File(requireContext().getCacheDir(), "camera_captured.jpg");
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    capturedImageFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showSnackbar(binding.imageView, "Gagal membuka kamera.");
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Utils.showSnackbar(binding.imageView, "Pilih gambar terlebih dahulu.");
            return;
        }

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
            File file = new File(requireContext().getCacheDir(),
                    Utils.getFileName(requireContext().getContentResolver(), selectedImageUri));
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            UploadRequestBody requestBody = new UploadRequestBody(file, "image", this);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
            RequestBody desc = RequestBody.create(MediaType.parse("multipart/form-data"), "json");

            MyAPI api = MyAPI.create();
            api.uploadImage(body, desc).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Utils.showSnackbar(binding.imageView, "Gagal upload: " + t.getMessage());
                }

                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.body() != null) {
                        Utils.showSnackbar(binding.imageView, response.body().getMessage());
                    } else {
                        Utils.showSnackbar(binding.imageView, "Gagal mendapatkan respon dari server.");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showSnackbar(binding.imageView, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        // Tambahkan progress bar kalau perlu
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}