package com.example.noreact.ui.scan;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RoboflowResponse {
    @SerializedName("predictions")
    private List<Prediction> predictions;

    @SerializedName("time")
    private double time;

    @SerializedName("image")
    private ImageInfo image;

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public double getTime() {
        return time;
    }

    public ImageInfo getImage() {
        return image;
    }

    public static class Prediction {
        @SerializedName("class")
        private String className;

        @SerializedName("confidence")
        private double confidence;

        @SerializedName("x")
        private double x;

        @SerializedName("y")
        private double y;

        @SerializedName("width")
        private double width;

        @SerializedName("height")
        private double height;

        public String getClassName() {
            return className;
        }

        public double getConfidence() {
            return confidence;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
    }

    public static class ImageInfo {
        @SerializedName("width")
        private int width;

        @SerializedName("height")
        private int height;

        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
}