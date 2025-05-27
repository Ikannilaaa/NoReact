package com.example.noreact.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noreact.MainActivity;
import com.example.noreact.R;

import java.util.ArrayList;
import java.util.List;

public class AllergySelectionActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private final List<String> selectedAllergens = new ArrayList<>();
    private final String[] allergens = {
            "Crustaceans", "Eggs", "Fish", "Peanut", "Soybeans", "Milk",
            "Nuts", "Gluten", "Celery", "Mustard", "Sesame Seeds",
            "Sulphur Dioxide", "Molluscs", "Lupin"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergy_selection);

        gridLayout = findViewById(R.id.grid_allergens);

        for (String allergen : allergens) {
            addAllergenOption(allergen);
        }

        findViewById(R.id.btn_continue).setOnClickListener(v -> {
            Toast.makeText(this,
                    "Selected: " + selectedAllergens.toString(),
                    Toast.LENGTH_SHORT).show();

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("onboarding_done", true).apply();

            Intent intent = new Intent(AllergySelectionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void addAllergenOption(String allergenName) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_allergen, gridLayout, false);
        TextView label = view.findViewById(R.id.label);
        label.setText(allergenName);

        ImageView icon = view.findViewById(R.id.icon);

        LinearLayout itemLayout = view.findViewById(R.id.allergenItem);
        itemLayout.setOnClickListener(v -> {
            boolean selected = !v.isSelected();
            v.setSelected(selected);

            v.setBackgroundResource(selected ? R.drawable.allergen_circle_selected : 0);

            if (selected) {
                selectedAllergens.add(allergenName);
            } else {
                selectedAllergens.remove(allergenName);
            }
        });

        gridLayout.addView(view);
    }
}