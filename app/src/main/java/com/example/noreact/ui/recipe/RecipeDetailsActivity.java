package com.example.noreact.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noreact.R;
import com.example.noreact.model.RecipeItem;

public class RecipeDetailsActivity extends AppCompatActivity {

    ImageView recipeImage;
    TextView recipeName, duration, allergenInformation, recipeContent, cookingInstruction;
     private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

         backButton = findViewById(R.id.backButtonOnDetail);
         if (backButton != null) {
             backButton.setOnClickListener(v -> onBackPressed());
         }

        recipeImage = findViewById(R.id.recipeImage);
        recipeName = findViewById(R.id.recipeName);
        duration = findViewById(R.id.duration);
        allergenInformation = findViewById(R.id.allergenInformation);
        recipeContent = findViewById(R.id.recipeContent);
        cookingInstruction = findViewById(R.id.cookingInstruction);

        Intent intent = getIntent();
        if (intent != null) {
            RecipeItem recipe = (RecipeItem) intent.getSerializableExtra("recipe");

            if (recipe != null) {
                recipeName.setText(recipe.getName());
                duration.setText(recipe.getDuration());
                allergenInformation.setText("⚠️ " + recipe.getAllergen());

                if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                    recipeContent.setText(android.text.TextUtils.join("\n", recipe.getIngredients()));
                } else {
                    recipeContent.setText("No ingredients listed.");
                }

                if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
                    cookingInstruction.setText(android.text.TextUtils.join("\n", recipe.getInstructions()));
                } else {
                    cookingInstruction.setText("No instructions available.");
                }


                String imageName = recipe.getImage();
                if (imageName != null && !imageName.isEmpty()) {
                    int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    if (imageResId != 0) {
                        recipeImage.setImageResource(imageResId);
                    } else {
                        recipeImage.setImageResource(R.drawable.error_image);
                        android.util.Log.e("RecipeDetails", "Drawable not found for: " + imageName);
                    }
                } else {
                    recipeImage.setImageResource(R.drawable.error_image);
                }
            }
        }
    }
}