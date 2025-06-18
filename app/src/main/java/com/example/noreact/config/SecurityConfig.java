// SecurityConfig.java
package com.example.noreact.config;

public class SecurityConfig {

    // PERINGATAN: Jangan hardcode API key di production!
    // Gunakan BuildConfig atau server-side proxy untuk keamanan

    // Untuk development
    public static final String API_KEY = "8c293618f19d4e2d8b1e9b8c801c8a18";

    // Untuk production, gunakan salah satu metode berikut:

    // 1. Menggunakan BuildConfig (set di build.gradle)
    // public static final String API_KEY = BuildConfig.AIML_API_KEY;

    // 2. Menggunakan server proxy (recommended)
    // public static final String PROXY_BASE_URL = "https://yourserver.com/api/";

    // 3. Menggunakan environment variables atau secure storage
    // public static String getApiKey(Context context) {
    //     // Implement secure storage retrieval
    //     return SecurePreferences.getString(context, "api_key", "");
    // }

    public static final String BASE_URL = "https://api.aimlapi.com";
    public static final String DEFAULT_MODEL = "gpt-4o";
    public static final int MAX_TOKENS = 1000;
    public static final double TEMPERATURE = 0.7;
    public static final int REQUEST_TIMEOUT = 30; // seconds
}

// Untuk production, tambahkan di build.gradle:
/*
android {
    buildTypes {
        debug {
            buildConfigField "String", "AIML_API_KEY", "\"your_api_key_here\""
        }
        release {
            buildConfigField "String", "AIML_API_KEY", "\"your_api_key_here\""
            // Atau gunakan keystore properties untuk keamanan lebih
        }
    }
}
*/