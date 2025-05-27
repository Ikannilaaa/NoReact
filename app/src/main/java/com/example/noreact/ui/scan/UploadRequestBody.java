package com.example.noreact.ui.scan;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class UploadRequestBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    private final File file;
    private final String contentType;
    private final UploadCallback callback;

    public UploadRequestBody(File file, String contentType, UploadCallback callback) {
        this.file = file;
        this.contentType = contentType;
        this.callback = callback;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType + "/*");
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream inputStream = new FileInputStream(file);
        long uploaded = 0;

        Handler handler = new Handler(Looper.getMainLooper());

        try {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                handler.post(new ProgressUpdater(uploaded, fileLength));
                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            inputStream.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private final long uploaded;
        private final long total;

        public ProgressUpdater(long uploaded, long total) {
            this.uploaded = uploaded;
            this.total = total;
        }

        @Override
        public void run() {
            if (callback != null) {
                int percent = (int) (100 * uploaded / total);
                callback.onProgressUpdate(percent);
            }
        }
    }

    public interface UploadCallback {
        void onProgressUpdate(int percentage);
    }
}
