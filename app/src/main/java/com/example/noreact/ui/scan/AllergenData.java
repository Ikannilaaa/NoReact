package com.example.noreact.ui.scan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllergenData {
    private static final Map<String, List<String>> ALLERGEN_MAP = new HashMap<>();

    static {
        ALLERGEN_MAP.put("ayam_geprek", Arrays.asList("telur", "gluten", "cabai"));
        ALLERGEN_MAP.put("ayam_goreng", Arrays.asList("gluten"));
        ALLERGEN_MAP.put("ayam_pop", Arrays.asList("santan"));
        ALLERGEN_MAP.put("cumi_goreng", Arrays.asList("seafood", "gluten"));
        ALLERGEN_MAP.put("daging_rendang", Arrays.asList("santan", "cabai"));
        ALLERGEN_MAP.put("dendeng_batokok", Arrays.asList("cabai"));
        ALLERGEN_MAP.put("gado_gado", Arrays.asList("kacang", "telur", "kedelai", "kecambah", "cabai"));
        ALLERGEN_MAP.put("gulai_ayam", Arrays.asList("santan", "cabai"));
        ALLERGEN_MAP.put("gulai_ikan", Arrays.asList("santan", "seafood", "cabai"));
        ALLERGEN_MAP.put("gulai_tambusu", Arrays.asList("telur", "santan", "cabai"));
        ALLERGEN_MAP.put("gulai_tunjang", Arrays.asList("santan", "cabai"));
        ALLERGEN_MAP.put("ikan_goreng", Arrays.asList("seafood"));
        ALLERGEN_MAP.put("kentang_goreng", Arrays.asList("gluten"));
        ALLERGEN_MAP.put("lontong_kupang", Arrays.asList("kerang", "gluten", "cabai"));
        ALLERGEN_MAP.put("lumpia", Arrays.asList("gluten", "telur", "kedelai", "cabai"));
        ALLERGEN_MAP.put("mie_goreng", Arrays.asList("gluten", "telur", "kedelai", "cabai"));
        ALLERGEN_MAP.put("nasi_goreng", Arrays.asList("telur", "kedelai", "cabai"));
        ALLERGEN_MAP.put("pecel_sayuran", Arrays.asList("kacang", "kecambah", "cabai"));
        ALLERGEN_MAP.put("rawon", Arrays.asList("kedelai"));
        ALLERGEN_MAP.put("sate", Arrays.asList("kacang", "kedelai", "cabai"));
        ALLERGEN_MAP.put("soto", Arrays.asList("santan", "telur", "kecambah", "cabai"));
        ALLERGEN_MAP.put("telur_balado", Arrays.asList("telur", "cabai"));
        ALLERGEN_MAP.put("telur_dadar", Arrays.asList("telur"));
    }

    public static List<String> getAllergens(String foodName) {
        return ALLERGEN_MAP.get(foodName.toLowerCase().replace(" ", "_"));
    }

    public static String formatFoodName(String foodName) {
        return foodName.replace("_", " ").toUpperCase().charAt(0) +
                foodName.replace("_", " ").substring(1).toLowerCase();
    }
}