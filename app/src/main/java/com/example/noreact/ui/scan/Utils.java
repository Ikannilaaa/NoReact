package com.example.noreact.ui.scan;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Utils {

    // Menampilkan snackbar
    public static void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("Ok", v -> snackbar.dismiss());
        snackbar.show();
    }

    // Mengambil nama file dari Uri
    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String name = "";
        Cursor returnCursor = contentResolver.query(fileUri, null, null, null, null);
        if (returnCursor != null) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            name = returnCursor.getString(nameIndex);
            returnCursor.close();
        }
        return name;
    }
}
