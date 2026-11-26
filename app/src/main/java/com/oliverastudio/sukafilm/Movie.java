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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    private int id;
    private String title;
    private String originalTitle;
    private int year;
    private String duration;
    private double rating;
    private String quality;
    private String genre;
    private String country = "Batı";
    private int trendingRank = 0;
    private String description;
    private String director;
    private String cast;
    private String posterUrl;
    private String backdropUrl;
    private String streamUrl;
    private String download1080p;
    private String download720p;
    private String download480p;
    private String size1080p;
    private String size720p;
    private String size480p;
    private List<Season> seasons = new ArrayList<>();

    public Movie() {
        this.seasons = new ArrayList<>();
    }

    public Movie(int id, String title, String originalTitle, int year, String duration,
                 double rating, String quality, String genre, String description,
                 String director, String cast, String posterUrl, String backdropUrl,
                 String streamUrl, String download1080p, String download720p,
                 String download480p, String size1080p, String size720p, String size480p) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.year = year;
        this.duration = duration;
        this.rating = rating;
        this.quality = quality;
        this.genre = genre;
        this.description = description;
        this.director = director;
        this.cast = cast;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.streamUrl = streamUrl;
        this.download1080p = download1080p;
        this.download720p = download720p;
        this.download480p = download480p;
        this.size1080p = size1080p;
        this.size720p = size720p;
        this.size480p = size480p;
        this.seasons = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    /** M3U entries often have no year or rating; show a dash instead of 0. */
    public String getYearText() { return year > 0 ? String.valueOf(year) : "-"; }
    public String getRatingText() { return rating > 0 ? String.valueOf(rating) : "-"; }

    public String getCountry() { return country != null ? country : "Batı"; }
    public void setCountry(String country) { this.country = country; }

    public int getTrendingRank() { return trendingRank; }
    public void setTrendingRank(int trendingRank) { this.trendingRank = trendingRank; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public String getDownload1080p() { return download1080p; }
    public void setDownload1080p(String download1080p) { this.download1080p = download1080p; }

    public String getDownload720p() { return download720p; }
    public void setDownload720p(String download720p) { this.download720p = download720p; }

    public String getDownload480p() { return download480p; }
    public void setDownload480p(String download480p) { this.download480p = download480p; }

    public String getSize1080p() { return size1080p; }
    public void setSize1080p(String size1080p) { this.size1080p = size1080p; }

    public String getSize720p() { return size720p; }
    public void setSize720p(String size720p) { this.size720p = size720p; }

    public String getSize480p() { return size480p; }
    public void setSize480p(String size480p) { this.size480p = size480p; }

    public List<Season> getSeasons() { return seasons; }
    public void setSeasons(List<Season> seasons) { this.seasons = seasons != null ? seasons : new ArrayList<>(); }
    public boolean hasSeasons() { return seasons != null && !seasons.isEmpty(); }
}