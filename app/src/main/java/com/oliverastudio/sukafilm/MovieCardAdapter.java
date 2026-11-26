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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class MovieCardAdapter extends RecyclerView.Adapter<MovieCardAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private final Context context;
    private List<Movie> movies;
    private final OnMovieClickListener listener;
    private final boolean isGrid;

    public MovieCardAdapter(Context context, List<Movie> movies, boolean isGrid, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies != null ? movies : new ArrayList<>();
        this.isGrid = isGrid;
        this.listener = listener;
    }

    public void updateList(List<Movie> newMovies) {
        this.movies = newMovies != null ? newMovies : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGrid ? R.layout.item_movie_grid_card : R.layout.item_movie_card;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.txtTitle.setText(movie.getTitle());
        holder.txtGenreYear.setText((movie.getGenre() != null ? movie.getGenre() : "Film") + " • " + movie.getYearText());
        holder.txtRating.setText(movie.getRatingText());

        String quality = movie.getQuality();
        if (quality != null && quality.toUpperCase().contains("4K")) {
            holder.txtQuality.setText("4K");
        } else if (quality != null && quality.toUpperCase().contains("1080P")) {
            holder.txtQuality.setText("1080P");
        } else {
            holder.txtQuality.setText("HD");
        }

        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.bg_badge_dark)
                .error(R.drawable.bg_badge_dark)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtQuality;
        TextView txtRating;
        TextView txtTitle;
        TextView txtGenreYear;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_movie_poster);
            txtQuality = itemView.findViewById(R.id.txt_movie_quality);
            txtRating = itemView.findViewById(R.id.txt_movie_rating);
            txtTitle = itemView.findViewById(R.id.txt_movie_title);
            txtGenreYear = itemView.findViewById(R.id.txt_movie_genre_year);
        }
    }
}