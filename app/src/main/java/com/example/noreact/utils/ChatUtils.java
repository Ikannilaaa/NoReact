package com.example.noreact.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatUtils {

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static boolean isToday(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String today = sdf.format(new Date());
        String messageDate = sdf.format(new Date(timestamp));
        return today.equals(messageDate);
    }

    public static String sanitizeMessage(String message) {
        if (message == null) return "";
        return message.trim().replaceAll("\\s+", " ");
    }

    public static boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty() && message.trim().length() <= 2000;
    }
}