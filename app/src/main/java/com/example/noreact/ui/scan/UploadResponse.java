package com.example.noreact.ui.scan;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    @SerializedName("error")
    private boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("image")
    private String image;

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getImage() {
        return image;
    }
}
