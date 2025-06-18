package com.example.noreact.model;

import java.util.Date;
import java.util.List;

public class HistoryItem {
    private String id;
    private String userId;
    private String foodName;
    private String imageBase64;
    private List<String> allergens;
    private double confidence;
    private String status; // "success", "no_food", "error"
    private Date scanDate;

    // Default constructor untuk Firebase
    public HistoryItem() {}

    // Constructor
    public HistoryItem(String userId, String foodName, String imageBase64,
                       List<String> allergens, double confidence, String status, Date scanDate) {
        this.userId = userId;
        this.foodName = foodName;
        this.imageBase64 = imageBase64;
        this.allergens = allergens;
        this.confidence = confidence;
        this.status = status;
        this.scanDate = scanDate;
    }

    // Getters dan Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getScanDate() { return scanDate; }
    public void setScanDate(Date scanDate) { this.scanDate = scanDate; }
}