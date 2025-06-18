package com.example.noreact.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noreact.R;
import com.example.noreact.model.RecipeItem;
import com.example.noreact.ui.recipe.RecipeDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private Context context;
    private List<RecipeItem> recipeList = new ArrayList<>();

    public RecipeAdapter(Context context, List<RecipeItem> recipeList) {
        this.context = context;
        if (recipeList != null) {
            this.recipeList = recipeList;
        }
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder holder, int position) {
        RecipeItem item = recipeList.get(position);

        holder.recipeName.setText(item.getName());
        holder.duration.setText(item.getDuration());
        holder.allergen.setText("⚠️ " + item.getAllergen());
        List<String> ingredients = item.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            holder.recipeContent.setText(android.text.TextUtils.join(", ", ingredients));
        } else {
            holder.recipeContent.setText("No ingredients listed.");
        }
        int imageId = context.getResources().getIdentifier(item.getImage(), "drawable", context.getPackageName());
        if (imageId != 0) {
            holder.recipeImage.setImageResource(imageId);
        } else {
//            holder.recipeImage.setImageResource(R.drawable.default_recipe_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra("recipe", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName, duration, allergen, recipeContent;

        public ViewHolder(View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            duration = itemView.findViewById(R.id.duration);
            allergen = itemView.findViewById(R.id.allergenInformation);
            recipeContent = itemView.findViewById(R.id.recipeContent);
        }
    }
}