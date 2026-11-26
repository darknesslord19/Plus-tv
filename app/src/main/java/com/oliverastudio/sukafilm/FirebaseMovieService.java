// ============================================================
//   Project Name  : SUKA FILM
//   Author        : Celine Olivia Clarissa
//   Studio        : Olivera Studio
//   Copyright (c) : 2026 Olivera Studio. All rights reserved.
//   Description   : Turning coffee into code, one line at a time.
// ============================================================
//   Terms & Conditions :
//   - This code is NOT for sale or commercial redistribution.
//   - Modifying, copying, or redistributing any part of this
//     code requires prior written permission from the developer.
//   - For inquiries, please contact the author directly.
// ============================================================
//   Crafted with logic, love, and a dash of magic.
// ============================================================

package com.oliverastudio.sukafilm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseMovieService {
    private static final String TAG = "FirebaseMovieService";

    private final DatabaseReference moviesRef;
    private final Handler mainHandler;

    public interface OnDownloadUrlResolvedListener {
        void onResolved(String downloadUrl);
    }

    public FirebaseMovieService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        moviesRef = database.getReference("movies");
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void resolveDownloadUrl(
            int movieId,
            String quality,
            String defaultUrl,
            OnDownloadUrlResolvedListener listener
    ) {
        moviesRef.child(String.valueOf(movieId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String url = null;

                        if (snapshot.exists()) {
                            if (quality != null) {
                                if (quality.contains("1080")) {
                                    url = snapshot.child("download1080p")
                                            .getValue(String.class);
                                } else if (quality.contains("720")) {
                                    url = snapshot.child("download720p")
                                            .getValue(String.class);
                                } else if (quality.contains("480")) {
                                    url = snapshot.child("download480p")
                                            .getValue(String.class);
                                }
                            }

                            if (!isValidUrl(url)) {
                                url = snapshot.child("downloadUrl")
                                        .getValue(String.class);
                            }

                            if (!isValidUrl(url)) {
                                url = snapshot.child("sourceUrl")
                                        .getValue(String.class);
                            }
                        }

                        final String result = isValidUrl(url) ? url : defaultUrl;

                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onResolved(result);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w(TAG, "Gagal mengambil data Firebase: " + error.getMessage());
                        fallback(defaultUrl, listener);
                    }
                });
    }

    private boolean isValidUrl(String url) {
        return url != null && !url.trim().isEmpty() && (url.startsWith("https://") || url.startsWith("http://"));
    }

    private void fallback(
            String defaultUrl,
            OnDownloadUrlResolvedListener listener
    ) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onResolved(defaultUrl);
            }
        });
    }
}