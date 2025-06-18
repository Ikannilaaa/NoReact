package com.example.noreact.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    public static String getNetworkErrorMessage(Context context) {
        if (!isNetworkAvailable(context)) {
            return "Tidak ada koneksi internet. Periksa koneksi Anda dan coba lagi.";
        }
        return "Terjadi kesalahan jaringan. Silakan coba lagi.";
    }
}