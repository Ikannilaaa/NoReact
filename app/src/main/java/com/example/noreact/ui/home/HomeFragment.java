package com.example.noreact.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.noreact.R;
import com.example.noreact.model.HistoryItem;
import com.example.noreact.ui.recipe.Recipe;
import com.example.noreact.ui.history.HistoryDetailActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.text.TextUtils;


public class HomeFragment extends Fragment {

    private SearchView homeSearchView;
    private CardView cardRecentlyAssessedItem;
    private TextView tvRecentlyAssessedViewAll;
    private HistoryItem latestHistoryItem;

    private LinearLayout chiliFree, eggFree, glutenFree, coconutMilkFree,
            seafoodFree, nutFree, soyaFree, beanSproutFree, clamFree;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private ImageView ivFoodImage;
    private TextView tvFoodName;
    private TextView tvScanDate;
    private TextView tvStatus;
    private TextView tvConfidence;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initFirebase();
        initViews(view);
        setupClickListeners(view);
        loadLatestHistoryItem();

        return view;
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        homeSearchView = view.findViewById(R.id.home_search_view);
        cardRecentlyAssessedItem = view.findViewById(R.id.cardRecentlyAssessedItem);
        tvRecentlyAssessedViewAll = view.findViewById(R.id.tvRecentlyAssessedViewAll);

        ivFoodImage = view.findViewById(R.id.ivFoodImage);
        tvFoodName = view.findViewById(R.id.tvFoodName);
        tvScanDate = view.findViewById(R.id.tvScanDate);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvConfidence = view.findViewById(R.id.tvConfidence);

        chiliFree = view.findViewById(R.id.chiliFree);
        eggFree = view.findViewById(R.id.eggFree);
        glutenFree = view.findViewById(R.id.glutenFree);
        coconutMilkFree = view.findViewById(R.id.coconutMilkFree);
        seafoodFree = view.findViewById(R.id.seafoodFree);
        nutFree = view.findViewById(R.id.nutFree);
        soyaFree = view.findViewById(R.id.soyFree);
        beanSproutFree = view.findViewById(R.id.beanSproutFree);
        clamFree = view.findViewById(R.id.clamFree);

        cardRecentlyAssessedItem.setVisibility(View.GONE);
    }

    private void setupClickListeners(View view) {
        if (homeSearchView != null) {
            homeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Intent intent = new Intent(getActivity(), Recipe.class);
                    intent.putExtra("search_query", query);
                    startActivity(intent);
                    homeSearchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }

        tvRecentlyAssessedViewAll.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_history, null,
                    new NavOptions.Builder()
                            .setPopUpTo(R.id.navigation_home, true)
                            .build());
        });


        if (cardRecentlyAssessedItem != null) {
            cardRecentlyAssessedItem.setOnClickListener(v -> {
                if (latestHistoryItem != null && getContext() != null) {
                    Intent detailIntent = new Intent(getContext(), HistoryDetailActivity.class);
                    detailIntent.putExtra("history_id", latestHistoryItem.getId());
                    startActivity(detailIntent);
                } else {
                    Toast.makeText(getContext(), "No recent item to show details.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setupAllergenIconListeners(view);
    }

    private void setupAllergenIconListeners(View view) {
        chiliFree.setOnClickListener(v -> navigateToRecipeWithExclusion("cabai"));
        eggFree.setOnClickListener(v -> navigateToRecipeWithExclusion("telur"));
        glutenFree.setOnClickListener(v -> navigateToRecipeWithExclusion("gluten"));
        coconutMilkFree.setOnClickListener(v -> navigateToRecipeWithExclusion("santan"));
        seafoodFree.setOnClickListener(v -> navigateToRecipeWithExclusion("seafood"));
        nutFree.setOnClickListener(v -> navigateToRecipeWithExclusion("kacang"));
        soyaFree.setOnClickListener(v -> navigateToRecipeWithExclusion("kedelai"));
        beanSproutFree.setOnClickListener(v -> navigateToRecipeWithExclusion("kecambah"));
        clamFree.setOnClickListener(v -> navigateToRecipeWithExclusion("kerang"));
    }

    private void navigateToRecipeWithExclusion(String allergenToExclude) {
        Intent intent = new Intent(getActivity(), Recipe.class);
        intent.putExtra("exclude_allergen_on_launch", allergenToExclude);
        startActivity(intent);
    }

    private void loadLatestHistoryItem() {
        if (auth.getCurrentUser() == null) {
            cardRecentlyAssessedItem.setVisibility(View.GONE);
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        firestore.collection("history")
                .whereEqualTo("userId", userId)
                .orderBy("scanDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        latestHistoryItem = document.toObject(HistoryItem.class);
                        latestHistoryItem.setId(document.getId());

                        displayLatestHistoryItem(latestHistoryItem);
                        cardRecentlyAssessedItem.setVisibility(View.VISIBLE);
                    } else {
                        cardRecentlyAssessedItem.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load recent history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cardRecentlyAssessedItem.setVisibility(View.GONE);
                    Log.e("HomeFragment", "Error loading latest history: " + e.getMessage(), e);
                });
    }

    private void displayLatestHistoryItem(HistoryItem item) {
        if (item == null) {
            cardRecentlyAssessedItem.setVisibility(View.GONE);
            return;
        }

        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivFoodImage.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                ivFoodImage.setImageResource(R.drawable.placeholder_food);
                Log.e("HomeFragment", "Invalid Base64 for history image: " + e.getMessage());
            }
        } else {
            ivFoodImage.setImageResource(R.drawable.placeholder_food);
        }

        tvFoodName.setText(formatFoodName(item.getFoodName()));

        if (item.getScanDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, HH:mm", new Locale("id", "ID"));
            tvScanDate.setText(sdf.format(item.getScanDate()));
        } else {
            tvScanDate.setText("Date N/A");
        }

        String statusText = item.getStatus() != null ? item.getStatus() : "";
        if ("success".equalsIgnoreCase(statusText)) {
            tvStatus.setText("✅ Berhasil");
        } else if ("no_food".equalsIgnoreCase(statusText)) {
            tvStatus.setText("❌ Bukan Makanan");
        } else if ("error".equalsIgnoreCase(statusText)) {
            tvStatus.setText("⚠️ Error");
        }

        if (item.getConfidence() != 0) {
            tvConfidence.setText(String.format(Locale.getDefault(), "Kepercayaan: %.1f%%", item.getConfidence() * 100));
        } else {
            tvConfidence.setText("Kepercayaan: N/A");
        }
    }

    private String formatFoodName(String foodName) {
        if (foodName == null) return "Unknown";

        String[] words = foodName.replace("_", " ").split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (formatted.length() > 0) formatted.append(" ");
            if (word.length() > 0) {
                formatted.append(word.substring(0, 1).toUpperCase(Locale.getDefault()))
                        .append(word.substring(1).toLowerCase(Locale.getDefault()));
            }
        }
        return formatted.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLatestHistoryItem();
    }
}