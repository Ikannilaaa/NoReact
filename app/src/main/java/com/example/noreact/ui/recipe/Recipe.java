package com.example.noreact.ui.recipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noreact.R;
import com.example.noreact.adapter.RecipeAdapter;
import com.example.noreact.model.RecipeItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Recipe extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<RecipeItem> recipeList;
    private List<RecipeItem> originalRecipeList;
    private ImageButton backButton;
    private SearchView searchView;
    private ImageButton filterButton;

    // Capitalize the allergens directly in the array
    private final String[] ALLERGENS = {"Cabai", "Telur", "Gluten", "Santan", "Seafood", "Kacang", "Kedelai", "Kecambah", "Kerang"};
    private final Set<String> selectedAllergens = new HashSet<>(); // Stores selected allergens in their original (capitalized) form

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        searchView = findViewById(R.id.searchView);
        filterButton = findViewById(R.id.filterButton);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        originalRecipeList = loadRecipeList(this);
        recipeList = new ArrayList<>(originalRecipeList);

        RecipeAdapter adapter = new RecipeAdapter(this, recipeList);
        recyclerView.setAdapter(adapter);

        setupSearchView();
        setupFilterButton();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("exclude_allergen_on_launch")) {
                String allergenToExclude = intent.getStringExtra("exclude_allergen_on_launch");
                if (allergenToExclude != null) {
                    selectedAllergens.add(allergenToExclude.toLowerCase(Locale.getDefault()));
                }
            }
            if (intent.hasExtra("search_query")) {
                String searchQuery = intent.getStringExtra("search_query");
                if (searchQuery != null) {
                    searchView.setQuery(searchQuery, false);
                }
            }
        }
        applyFilters();
    }

    private List<RecipeItem> loadRecipeList(Context context) {
        List<RecipeItem> loadedList = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("recipes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Log.d("RecipeDebug", "JSON content: " + json);

            JSONArray recipesArray = new JSONArray(json);

            for (int i = 0; i < recipesArray.length(); i++) {
                JSONObject recipeObj = recipesArray.getJSONObject(i);

                String name = recipeObj.getString("name");
                String imageUrl = recipeObj.getString("image");
                String duration = recipeObj.getString("duration");

                JSONArray ingredientsArray = recipeObj.getJSONArray("ingredients");
                List<String> ingredients = new ArrayList<>();
                for (int j = 0; j < ingredientsArray.length(); j++) {
                    ingredients.add(ingredientsArray.getString(j));
                }

                JSONArray allergensArray = recipeObj.getJSONArray("allergens");
                StringBuilder allergensBuilder = new StringBuilder();
                for (int l = 0; l < allergensArray.length(); l++) {
                    String allergen = allergensArray.getString(l);
                     allergensBuilder.append(capitalizeFirstLetter(allergen));
                    if (l < allergensArray.length() - 1) {
                        allergensBuilder.append(", ");
                    }
                }
                String allergens = allergensBuilder.toString();

                JSONArray instructionArray = recipeObj.getJSONArray("instructions");
                List<String> instruction = new ArrayList<>();
                for (int j = 0; j < instructionArray.length(); j++) {
                    instruction.add(instructionArray.getString(j));
                }

                RecipeItem currentRecipeItem = new RecipeItem(name, imageUrl, duration, ingredients, allergens, instruction);

                loadedList.add(currentRecipeItem);
            }

            Log.d("RecipeDebug", "Successfully parsed " + loadedList.size() + " recipes");

        } catch (Exception e) {
            Log.e("RecipeDebug", "Error loading recipes: " + e.getMessage(), e);
        }

        return loadedList;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1).toLowerCase(Locale.getDefault());
    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void setupFilterButton() {
        filterButton.setOnClickListener(v -> showAllergenFilterDialog());
    }

    private void showAllergenFilterDialog() {
        String[] displayAllergens = new String[ALLERGENS.length];
        for (int i = 0; i < ALLERGENS.length; i++) {
            displayAllergens[i] = capitalizeFirstLetter(ALLERGENS[i]);
        }

        boolean[] checkedItems = new boolean[ALLERGENS.length];
        for (int i = 0; i < ALLERGENS.length; i++) {
            checkedItems[i] = selectedAllergens.contains(ALLERGENS[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Filter by Allergens (Exclude)")
                .setMultiChoiceItems(displayAllergens, checkedItems, (dialog, which, isChecked) -> {
                    String allergenInOriginalCase = ALLERGENS[which]; // Use original case for internal logic
                    if (isChecked) {
                        selectedAllergens.add(allergenInOriginalCase);
                    } else {
                        selectedAllergens.remove(allergenInOriginalCase);
                    }
                })
                .setPositiveButton("Apply", (dialog, which) -> {
                    applyFilters();
                })
                .setNegativeButton("Clear All", (dialog, which) -> {
                    selectedAllergens.clear();
                    applyFilters();
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    private void applyFilters() {
        String currentQuery = searchView.getQuery().toString().toLowerCase(Locale.getDefault());
        recipeList.clear();

        for (RecipeItem item : originalRecipeList) {
            boolean matchesSearch = true;
            boolean containsExcludedAllergen = false;

            if (!currentQuery.isEmpty()) {
                matchesSearch = item.getName().toLowerCase(Locale.getDefault()).contains(currentQuery);
            }

            if (!selectedAllergens.isEmpty()) {
                List<String> itemAllergensLower = new ArrayList<>();
                if (item.getAllergen() != null && !item.getAllergen().isEmpty()) {
                    itemAllergensLower = Arrays.asList(item.getAllergen().toLowerCase(Locale.getDefault()).split(",\\s*"));
                }

                for (String selected : selectedAllergens) {
                    if (itemAllergensLower.contains(selected.toLowerCase(Locale.getDefault()))) { // Compare lowercase forms
                        containsExcludedAllergen = true;
                        break;
                    }
                }
            }

            if (matchesSearch && !containsExcludedAllergen) {
                recipeList.add(item);
            }
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}