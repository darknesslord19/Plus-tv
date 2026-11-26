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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieRepository {
    private static MovieRepository instance;
    private final List<Movie> movieList;

    private MovieRepository() {
        movieList = new ArrayList<>();
    }

    public static synchronized MovieRepository getInstance() {
        if (instance == null) {
            instance = new MovieRepository();
        }
        return instance;
    }

    public synchronized void setMovies(List<Movie> movies) {
        movieList.clear();
        if (movies != null) {
            movieList.addAll(movies);
        }
    }

    public synchronized List<Movie> getAllMovies() {
        return new ArrayList<>(movieList);
    }

    public synchronized Movie getMovieById(int id) {
        for (Movie m : movieList) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public synchronized List<Movie> getMoviesByGenre(String genre) {
        if (genre == null || genre.equalsIgnoreCase("Tümü") || genre.isEmpty()) {
            return getAllMovies();
        }
        List<Movie> filtered = new ArrayList<>();
        for (Movie m : movieList) {
            if (m.getGenre() != null && m.getGenre().equalsIgnoreCase(genre)) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    public synchronized List<Movie> searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMovies();
        }
        String lowerQuery = query.toLowerCase().trim();
        List<Movie> results = new ArrayList<>();
        for (Movie m : movieList) {
            boolean matchTitle = m.getTitle() != null && m.getTitle().toLowerCase().contains(lowerQuery);
            boolean matchOriginal = m.getOriginalTitle() != null && m.getOriginalTitle().toLowerCase().contains(lowerQuery);
            boolean matchGenre = m.getGenre() != null && m.getGenre().toLowerCase().contains(lowerQuery);
            boolean matchActor = m.getCast() != null && m.getCast().toLowerCase().contains(lowerQuery);
            boolean matchDirector = m.getDirector() != null && m.getDirector().toLowerCase().contains(lowerQuery);
            if (matchTitle || matchOriginal || matchGenre || matchActor || matchDirector) {
                results.add(m);
            }
        }
        return results;
    }
}