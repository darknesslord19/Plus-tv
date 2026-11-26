package com.oliverastudio.sukafilm;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/** Initializes Firebase from build-time values (replaces Sketchware's library settings). */
public class SukaFilmApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!FirebaseApp.getApps(this).isEmpty()) return;

        if (BuildConfig.FIREBASE_DATABASE_URL.isEmpty()) {
            Log.w("SukaFilmApp", "Firebase is not configured; no movies will load.");
        }

        // Placeholders let an unconfigured build open instead of crashing.
        FirebaseOptions.Builder options = new FirebaseOptions.Builder()
                .setApplicationId(orElse(BuildConfig.FIREBASE_APP_ID, "1:000000000000:android:0000000000000000"))
                .setApiKey(orElse(BuildConfig.FIREBASE_API_KEY, "not-configured"))
                .setDatabaseUrl(orElse(BuildConfig.FIREBASE_DATABASE_URL, "https://not-configured-default-rtdb.firebaseio.com"));
        if (!BuildConfig.FIREBASE_STORAGE_BUCKET.isEmpty()) {
            options.setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET);
        }
        FirebaseApp.initializeApp(this, options.build());
    }

    private static String orElse(String value, String fallback) {
        return value.isEmpty() ? fallback : value;
    }
}
