package com.example.noreact.ui.scan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noreact.R;
import java.util.List;

public class AllergenAdapter extends RecyclerView.Adapter<AllergenAdapter.AllergenViewHolder> {
    private List<String> allergens;

    public AllergenAdapter(List<String> allergens) {
        this.allergens = allergens;
    }

    @NonNull
    @Override
    public AllergenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_allergen, parent, false);
        return new AllergenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllergenViewHolder holder, int position) {
        String allergen = allergens.get(position);
        holder.allergenText.setText(allergen);

        // Set different colors for different allergens
        int colorRes = getColorForAllergen(allergen);
        holder.allergenText.setBackgroundResource(colorRes);
    }

    @Override
    public int getItemCount() {
        return allergens.size();
    }

    private int getColorForAllergen(String allergen) {
        switch (allergen.toLowerCase()) {
            case "telur":
                return R.drawable.allergen_egg;
            case "gluten":
                return R.drawable.allergen_gluten;
            case "seafood":
            case "kerang":
                return R.drawable.allergen_seafood;
            case "kacang":
                return R.drawable.allergen_nuts;
            case "kedelai":
                return R.drawable.allergen_soy;
            case "santan":
                return R.drawable.allergen_milk;
            case "cabai":
                return R.drawable.allergen_spicy;
            default:
                return R.drawable.allergen_default;
        }
    }

    static class AllergenViewHolder extends RecyclerView.ViewHolder {
        TextView allergenText;

        public AllergenViewHolder(@NonNull View itemView) {
            super(itemView);
            allergenText = itemView.findViewById(R.id.allergenText);
        }
    }
}