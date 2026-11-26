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

public class TrendingCardAdapter extends RecyclerView.Adapter<TrendingCardAdapter.TrendingViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private final Context context;
    private final List<Movie> movieList = new ArrayList<>();
    private final OnMovieClickListener listener;

    public TrendingCardAdapter(Context context, OnMovieClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<Movie> newMovies) {
        movieList.clear();
        if (newMovies != null) {
            movieList.addAll(newMovies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trending_card, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.txtRank.setText("#" + (position + 1));
        holder.txtTitle.setText(movie.getTitle());
        holder.txtGenreYear.setText((movie.getGenre() != null ? movie.getGenre() : "Film") + " • " + movie.getYearText());
        holder.txtRating.setText(movie.getRatingText());

        String quality = movie.getQuality();
        if (quality != null && quality.toUpperCase().contains("4K")) {
            holder.txtQuality.setText("4K");
            holder.txtQuality.setVisibility(View.VISIBLE);
        } else if (quality != null && quality.toUpperCase().contains("1080P")) {
            holder.txtQuality.setText("1080P");
            holder.txtQuality.setVisibility(View.VISIBLE);
        } else {
            holder.txtQuality.setText("HD");
            holder.txtQuality.setVisibility(View.VISIBLE);
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
        return movieList.size();
    }

    static class TrendingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtRank;
        TextView txtTitle;
        TextView txtGenreYear;
        TextView txtRating;
        TextView txtQuality;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_trending_poster);
            txtRank = itemView.findViewById(R.id.txt_trending_rank);
            txtTitle = itemView.findViewById(R.id.txt_trending_title);
            txtGenreYear = itemView.findViewById(R.id.txt_trending_genre_year);
            txtRating = itemView.findViewById(R.id.txt_trending_rating);
            txtQuality = itemView.findViewById(R.id.txt_trending_quality);
        }
    }
}