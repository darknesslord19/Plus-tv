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

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FavoritesHistoryManager {
    private static final String PREF_NAME = "suka_film_user_data";
    private static final String KEY_FAVORITES = "key_favorite_movie_ids";
    private static final String KEY_HISTORY = "key_history_movie_ids";

    private static FavoritesHistoryManager instance;
    private final SharedPreferences prefs;
    private final List<OnUserDataChangedListener> listeners = new ArrayList<>();

    public interface OnUserDataChangedListener {
        void onUserDataChanged();
    }

    private FavoritesHistoryManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FavoritesHistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesHistoryManager(context);
        }
        return instance;
    }

    public void addListener(OnUserDataChangedListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnUserDataChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnUserDataChangedListener l : listeners) {
            l.onUserDataChanged();
        }
    }

    public boolean isFavorite(int movieId) {
        Set<String> favs = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        return favs != null && favs.contains(String.valueOf(movieId));
    }

    public boolean toggleFavorite(int movieId) {
        Set<String> favs = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
        String idStr = String.valueOf(movieId);
        boolean isNowFav;
        if (favs.contains(idStr)) {
            favs.remove(idStr);
            isNowFav = false;
        } else {
            favs.add(idStr);
            isNowFav = true;
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply();
        notifyListeners();
        return isNowFav;
    }

    public List<Integer> getFavoriteMovieIds() {
        Set<String> favs = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        List<Integer> list = new ArrayList<>();
        if (favs != null) {
            for (String s : favs) {
                try {
                    list.add(Integer.parseInt(s));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    public void recordWatchHistory(int movieId) {
        String historyRaw = prefs.getString(KEY_HISTORY, "");
        String idStr = String.valueOf(movieId);
        List<String> items = new ArrayList<>();
        items.add(idStr);

        if (historyRaw != null && !historyRaw.isEmpty()) {
            String[] parts = historyRaw.split(",");
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty() && !trimmed.equals(idStr) && items.size() < 30) {
                    items.add(trimmed);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i));
        }

        prefs.edit().putString(KEY_HISTORY, sb.toString()).apply();
        notifyListeners();
    }

    public List<Integer> getWatchHistoryMovieIds() {
        String historyRaw = prefs.getString(KEY_HISTORY, "");
        List<Integer> list = new ArrayList<>();
        if (historyRaw != null && !historyRaw.isEmpty()) {
            String[] parts = historyRaw.split(",");
            for (String p : parts) {
                try {
                    list.add(Integer.parseInt(p.trim()));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    public void clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply();
        notifyListeners();
    }
}