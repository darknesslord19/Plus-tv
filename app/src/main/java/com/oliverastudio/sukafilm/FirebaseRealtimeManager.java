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

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FirebaseRealtimeManager {
    private static final String TAG = "FirebaseRealtimeMgr";
    private static FirebaseRealtimeManager instance;
    private final FirebaseDatabase database;
    private final DatabaseReference moviesRef;
    private final DatabaseReference genreRef;
    private final DatabaseReference bannersRef;
    private final List<Movie> currentMovies = new ArrayList<>();
    private final List<String> currentGenres = new ArrayList<>();
    private final List<OnMoviesDataChangedListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean isInitialDataLoaded = false;

    public interface OnMoviesDataChangedListener {
        void onDataChanged(List<Movie> movies, List<String> genres);
    }

    private FirebaseRealtimeManager() {
        database = FirebaseDatabase.getInstance();
        moviesRef = database.getReference("Movies");
        genreRef = database.getReference("Genre");
        bannersRef = database.getReference("Banners");
        initListeners();
    }

    public static synchronized FirebaseRealtimeManager getInstance() {
        if (instance == null) {
            instance = new FirebaseRealtimeManager();
        }
        return instance;
    }

    public synchronized void addListener(
            OnMoviesDataChangedListener listener) {
        if (listener == null) {
            return;
        }

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

        List<Movie> moviesSnapshot;
        List<String> genresSnapshot;

        synchronized (currentMovies) {
            moviesSnapshot = new ArrayList<>(currentMovies);
        }

        synchronized (currentGenres) {
            genresSnapshot = new ArrayList<>(currentGenres);
        }

        mainHandler.post(() -> listener.onDataChanged(moviesSnapshot, genresSnapshot));
    }

    public synchronized void removeListener(
            OnMoviesDataChangedListener listener) {
        listeners.remove(listener);
    }

    public boolean isInitialDataLoaded() {
        return isInitialDataLoaded;
    }

    private void initListeners() {
        moviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Movie> parsedMovies = new ArrayList<>();
                Log.d(TAG, "Data /Movies diterima, jumlah: " + snapshot.getChildrenCount());

                for (DataSnapshot movieSnap : snapshot.getChildren()) {
                    try {
                        Movie movie = parseMovieSnapshot(movieSnap);
                        if (movie != null) {
                            parsedMovies.add(movie);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Gagal parsing film " + movieSnap.getKey(), e);
                    }
                }

                synchronized (currentMovies) {
                    currentMovies.clear();
                    currentMovies.addAll(parsedMovies);
                }

                MovieRepository.getInstance().setMovies(
                        new ArrayList<>(parsedMovies));

                refreshGenresList();
                isInitialDataLoaded = true;
                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isInitialDataLoaded = true;
                Log.e(TAG, "Database error pada /Movies: " + error.getMessage());
                notifyListeners();
            }
        });

        genreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> genres = new LinkedHashSet<>();
                genres.add("Semua");

                for (DataSnapshot child : snapshot.getChildren()) {
                    Object value = child.getValue();
                    if (value instanceof String) {
                        String genre = ((String) value).trim();
                        if (!genre.isEmpty()) {
                            genres.add(genre);
                        }
                    }
                }

                synchronized (currentGenres) {
                    for (String genre : genres) {
                        if (!currentGenres.contains(genre)) {
                            currentGenres.add(genre);
                        }
                    }
                }

                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error pada /Genre: " + error.getMessage());
            }
        });
    }

    private Movie parseMovieSnapshot(DataSnapshot snap) {
        String key = snap.getKey();
        int id = 1;

        try {
            String numericKey = key != null ? key.replaceAll("[^0-9]", "") : "";
            if (!numericKey.isEmpty()) {
                id = Integer.parseInt(numericKey);
            } else {
                id = Math.abs(key != null ? key.hashCode() : 1);
            }
        } catch (Exception ignored) {
            id = 1;
        }

        String title = getStringField(
                snap, "Title", "title", "namaFilm", "namaGame", "Judul");
        if (title == null || title.isEmpty()) {
            title = "Film " + key;
        }

        String originalTitle = getStringField(
                snap, "OriginalTitle", "originalTitle", "original_title", "title");
        if (originalTitle == null) {
            originalTitle = title;
        }

        int year = getIntField(
                snap, "Year", "year", "tahun", 2024);

        String duration = getStringField(
                snap, "Duration", "duration", "durasi");
        if (duration == null) {
            duration = "2j 15m";
        }

        double rating = getDoubleField(
                snap, "Rating", "rating", "nilai", 8.5);

        String quality = getStringField(
                snap, "Quality", "quality", "kualitas");
        if (quality == null) {
            quality = "1080p Full HD";
        }

        String genre = getStringField(
                snap, "Genre", "genre", "kategori");
        if (genre == null) {
            genre = "Aksi";
        }

        String country = getStringField(
                snap, "Country", "country", "negara");
        if (country == null) {
            country = "Barat";
        }

        int trendingRank = getIntField(
                snap, "TrendingRank", "trendingRank", "trending", 0);

        String description = getStringField(
                snap, "Description", "description", "sinopsis", "deskripsi");
        if (description == null) {
            description = "Sinopsis film belum tersedia.";
        }

        String director = getStringField(
                snap, "Director", "director", "sutradara");
        if (director == null) {
            director = "Sutradara Tidak Diketahui";
        }

        String actor = getStringField(
                snap, "Actor", "actor", "actors", "cast", "pemeran");
        if (actor == null) {
            actor = "Pemeran Utama";
        }

        String cover = getStringField(
                snap, "Cover", "cover", "posterUrl", "gambarPoster", "poster");
        if (cover == null) {
            cover = "https://images.unsplash.com/"
                    + "photo-1536440136628-849c177e76a1"
                    + "?w=600&auto=format&fit=crop&q=80";
        }

        String backdrop = getStringField(
                snap, "Backdrop", "backdrop", "backdropUrl");
        if (backdrop == null) {
            backdrop = cover;
        }

        String streamUrl = getStringField(
                snap, "StreamUrl", "streamUrl", "stream_url", "sourceUrl");
        if (streamUrl == null) {
            streamUrl = "https://commondatastorage.googleapis.com/"
                    + "gtv-videos-bucket/sample/BigBuckBunny.mp4";
        }

        String download1080p = getStringField(
                snap, "download1080p", "1080p", "download_url", "sourceUrl");
        if (download1080p == null) {
            download1080p = streamUrl;
        }

        String download720p = getStringField(
                snap, "download720p", "720p");
        if (download720p == null) {
            download720p = download1080p;
        }

        String download480p = getStringField(
                snap, "download480p", "480p");
        if (download480p == null) {
            download480p = download720p;
        }

        String size1080p = getStringField(
                snap, "size1080p", "file_size");
        if (size1080p == null) {
            size1080p = "2.2 GB";
        }

        String size720p = getStringField(
                snap, "size720p");
        if (size720p == null) {
            size720p = "1.1 GB";
        }

        String size480p = getStringField(
                snap, "size480p");
        if (size480p == null) {
            size480p = "520 MB";
        }

        Movie movie = new Movie(
                id,
                title,
                originalTitle,
                year,
                duration,
                rating,
                quality,
                genre,
                description,
                director,
                actor,
                cover,
                backdrop,
                streamUrl,
                download1080p,
                download720p,
                download480p,
                size1080p,
                size720p,
                size480p
        );

        movie.setCountry(country);
        movie.setTrendingRank(trendingRank);

        List<Season> parsedSeasons = new ArrayList<>();
        DataSnapshot seasonsParent = snap.child("seasons");
        DataSnapshot targetSeasonParent = seasonsParent.exists() ? seasonsParent : snap;

        for (DataSnapshot seasonSnap : targetSeasonParent.getChildren()) {
            String seasonKey = seasonSnap.getKey();
            if (seasonKey == null) {
                continue;
            }

            String lowerKey = seasonKey.toLowerCase();
            boolean isSeason = lowerKey.contains("season")
                    || lowerKey.startsWith("s_")
                    || lowerKey.matches("s\\d+");

            if (!isSeason) {
                continue;
            }

            String seasonTitle = getStringField(
                    seasonSnap, "seasonTitle", "title");
            if (seasonTitle == null) {
                seasonTitle = seasonKey
                        .replace("_", " ")
                        .toUpperCase();
            }

            List<Episode> episodes = new ArrayList<>();

            DataSnapshot epContainer = seasonSnap.child("episodes").exists()
                    ? seasonSnap.child("episodes")
                    : seasonSnap;

            for (DataSnapshot epSnap : epContainer.getChildren()) {
                String epKey = epSnap.getKey();
                if (epKey == null) {
                    continue;
                }

                String epTitle = getStringField(
                        epSnap, "title");
                if (epTitle == null) {
                    epTitle = epKey
                            .replace("_", " ")
                            .toUpperCase();
                }

                String epThumb = getStringField(
                        epSnap, "thumbnail", "cover");
                if (epThumb == null) {
                    epThumb = cover;
                }

                String epDuration = getStringField(
                        epSnap, "duration");
                if (epDuration == null) {
                    epDuration = duration;
                }

                String epDownloadUrl = getStringField(
                        epSnap, "download_url", "downloadUrl", "streamUrl");
                if (epDownloadUrl == null) {
                    epDownloadUrl = download1080p;
                }

                String epSize = getStringField(
                        epSnap, "file_size", "size");
                if (epSize == null) {
                    epSize = size1080p;
                }

                String epQuality = getStringField(
                        epSnap, "quality");
                if (epQuality == null) {
                    epQuality = "1080p";
                }

                episodes.add(new Episode(
                        epKey,
                        epTitle,
                        epDuration,
                        epThumb,
                        epDownloadUrl,
                        epSize,
                        epQuality
                ));
            }

            if (!episodes.isEmpty()) {
                parsedSeasons.add(new Season(
                        seasonKey,
                        seasonTitle,
                        episodes
                ));
            }
        }

        if (parsedSeasons.isEmpty()) {
            List<Episode> episodes = new ArrayList<>();
            episodes.add(new Episode(
                    "ep_1",
                    "Full Movie (1080p)",
                    duration,
                    cover,
                    download1080p,
                    size1080p,
                    quality
            ));
            parsedSeasons.add(new Season(
                    "season_1",
                    "Full Movie",
                    episodes
            ));
        }

        movie.setSeasons(parsedSeasons);
        return movie;
    }

    private String getStringField(
            DataSnapshot snap, String... keys) {
        for (String key : keys) {
            if (snap.hasChild(key)) {
                Object value = snap.child(key).getValue();
                if (value != null) {
                    return String.valueOf(value);
                }
            }
        }
        return null;
    }

    private int getIntField(
            DataSnapshot snap, String key1, String key2, String key3, int fallback) {
        String[] keys = {key1, key2, key3};

        for (String key : keys) {
            if (!snap.hasChild(key)) {
                continue;
            }

            Object value = snap.child(key).getValue();
            if (value != null) {
                try {
                    return Integer.parseInt(
                            String.valueOf(value)
                                    .replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            }
        }

        return fallback;
    }

    private double getDoubleField(
            DataSnapshot snap, String key1, String key2, String key3, double fallback) {
        String[] keys = {key1, key2, key3};

        for (String key : keys) {
            if (!snap.hasChild(key)) {
                continue;
            }

            Object value = snap.child(key).getValue();
            if (value != null) {
                try {
                    return Double.parseDouble(
                            String.valueOf(value));
                } catch (Exception ignored) {
                }
            }
        }

        return fallback;
    }

    private void refreshGenresList() {
        Set<String> genres = new LinkedHashSet<>();
        genres.add("Semua");

        synchronized (currentMovies) {
            for (Movie movie : currentMovies) {
                String genre = movie.getGenre();
                if (genre != null && !genre.trim().isEmpty()) {
                    genres.add(genre.trim());
                }
            }
        }

        synchronized (currentGenres) {
            currentGenres.clear();
            currentGenres.addAll(genres);
        }
    }

    private void notifyListeners() {
        List<Movie> moviesSnapshot;
        List<String> genresSnapshot;
        List<OnMoviesDataChangedListener> listenerSnapshot;

        synchronized (currentMovies) {
            moviesSnapshot = new ArrayList<>(currentMovies);
        }

        synchronized (currentGenres) {
            genresSnapshot = new ArrayList<>(currentGenres);
        }

        synchronized (this) {
            listenerSnapshot = new ArrayList<>(listeners);
        }

        mainHandler.post(() -> {
            for (OnMoviesDataChangedListener listener : listenerSnapshot) {
                try {
                    listener.onDataChanged(
                            new ArrayList<>(moviesSnapshot),
                            new ArrayList<>(genresSnapshot)
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error saat memberi notifikasi listener", e);
                }
            }
        });
    }

    public List<Movie> getCurrentMovies() {
        synchronized (currentMovies) {
            return new ArrayList<>(currentMovies);
        }
    }

    public List<String> getCurrentGenres() {
        synchronized (currentGenres) {
            return new ArrayList<>(currentGenres);
        }
    }

    public Movie getMovieById(int id) {
        synchronized (currentMovies) {
            for (Movie movie : currentMovies) {
                if (movie.getId() == id) {
                    return movie;
                }
            }
        }

        return MovieRepository.getInstance().getMovieById(id);
    }
}