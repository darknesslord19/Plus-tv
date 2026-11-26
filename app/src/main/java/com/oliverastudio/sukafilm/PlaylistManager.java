package com.oliverastudio.sukafilm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Loads the M3U playlist and exposes it as movies grouped by group-title. */
public class PlaylistManager {
    private static final String TAG = "PlaylistManager";
    private static PlaylistManager instance;
    private static final String KEY_URL = "playlist_url";
    private static SharedPreferences preferences;

    private final List<Movie> currentMovies = new ArrayList<>();
    private final List<String> currentGenres = new ArrayList<>();
    private final List<OnMoviesDataChangedListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface OnMoviesDataChangedListener {
        void onDataChanged(List<Movie> movies, List<String> genres);
    }

    private PlaylistManager() {
        reload();
    }

    public static synchronized PlaylistManager getInstance() {
        if (instance == null) instance = new PlaylistManager();
        return instance;
    }

    static void init(Context context) {
        preferences = context.getSharedPreferences("playlist", Context.MODE_PRIVATE);
    }

    static void useDefaultUrl() {
        preferences.edit().remove(KEY_URL).apply();
    }

    /** The user's link, or the built-in default. */
    public String getPlaylistUrl() {
        return preferences.getString(KEY_URL, BuildConfig.PLAYLIST_URL);
    }

    /** Saves a new link (null restores the default) and loads it. Call on the main thread. */
    public void setPlaylistUrl(String url) {
        if (url == null) preferences.edit().remove(KEY_URL).apply();
        else preferences.edit().putString(KEY_URL, url).apply();
        publish(new ArrayList<>(), new ArrayList<>());
        reload();
    }

    /** Downloads the playlist again. On failure the last loaded list is kept. */
    public void reload() {
        final String url = getPlaylistUrl();
        executor.execute(() -> {
            try {
                List<Movie> movies = toMovies(M3uParser.parse(download(url)));
                Set<String> genres = new LinkedHashSet<>();
                genres.add("Tümü");
                for (Movie movie : movies) genres.add(movie.getGenre());
                mainHandler.post(() -> {
                    // Ignore a slow result if the link changed in the meantime.
                    if (url.equals(getPlaylistUrl())) publish(movies, new ArrayList<>(genres));
                });
            } catch (Exception e) {
                Log.e(TAG, "Playlist could not be loaded", e);
                mainHandler.post(this::notifyListeners);
            }
        });
    }

    private static String download(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setUseCaches(false);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + connection.getResponseCode());
            }
            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    if (out.size() > 10 * 1024 * 1024) throw new IOException("Playlist is over 10 MB");
                }
                return out.toString("UTF-8");
            }
        } finally {
            connection.disconnect();
        }
    }

    private static List<Movie> toMovies(List<M3uParser.Entry> entries) {
        List<Movie> movies = new ArrayList<>();
        Set<Integer> usedIds = new LinkedHashSet<>();
        for (M3uParser.Entry entry : entries) {
            // Stable across reloads, so favorites survive reordering of the playlist.
            int id = (entry.title + "|" + entry.url).hashCode() & 0x7fffffff;
            if (!usedIds.add(id)) continue;

            int year = 0;
            double rating = 0;
            try { year = Integer.parseInt(entry.attribute("year", "0")); } catch (NumberFormatException ignored) {}
            try { rating = Double.parseDouble(entry.attribute("rating", "0")); } catch (NumberFormatException ignored) {}

            String poster = entry.attribute("tvg-logo", "");
            String quality = entry.attribute("quality", "Video");
            String duration = entry.attribute("duration", "");
            String size = "Boyut bilinmiyor";

            Movie movie = new Movie(id, entry.title, entry.title, year, duration, rating, quality,
                    entry.group, entry.attribute("description", "Açıklama yok."), "-", "-",
                    poster, poster, entry.url, entry.url, entry.url, entry.url, size, size, size);
            movie.setCountry(entry.attribute("tvg-country", ""));

            List<Episode> episodes = new ArrayList<>();
            episodes.add(new Episode("ep_1", entry.title, duration, poster, entry.url, size, quality));
            List<Season> seasons = new ArrayList<>();
            seasons.add(new Season("season_1", "Video", episodes));
            movie.setSeasons(seasons);
            movies.add(movie);
        }
        return movies;
    }

    private void publish(List<Movie> movies, List<String> genres) {
        synchronized (this) {
            currentMovies.clear();
            currentMovies.addAll(movies);
            currentGenres.clear();
            currentGenres.addAll(genres);
        }
        MovieRepository.getInstance().setMovies(movies);
        notifyListeners();
    }

    private void notifyListeners() {
        for (OnMoviesDataChangedListener listener : new ArrayList<>(listeners)) {
            listener.onDataChanged(getCurrentMovies(), getCurrentGenres());
        }
    }

    /** Call on the main thread. */
    public void addListener(OnMoviesDataChangedListener listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
        mainHandler.post(() -> listener.onDataChanged(getCurrentMovies(), getCurrentGenres()));
    }

    /** Call on the main thread. */
    public void removeListener(OnMoviesDataChangedListener listener) {
        listeners.remove(listener);
    }

    public synchronized List<Movie> getCurrentMovies() {
        return new ArrayList<>(currentMovies);
    }

    public synchronized List<String> getCurrentGenres() {
        return new ArrayList<>(currentGenres);
    }

    public Movie getMovieById(int id) {
        for (Movie movie : getCurrentMovies()) {
            if (movie.getId() == id) return movie;
        }
        return MovieRepository.getInstance().getMovieById(id);
    }
}
