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

public class BannerParallaxAdapter extends RecyclerView.Adapter<BannerParallaxAdapter.BannerViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private final Context context;
    private final List<Movie> movies;
    private final OnMovieClickListener listener;

    public BannerParallaxAdapter(Context context, List<Movie> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies != null ? new ArrayList<>(movies) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Movie> newMovies) {
        this.movies.clear();
        if (newMovies != null) {
            this.movies.addAll(newMovies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner_parallax, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.txtTitle.setText(movie.getTitle());
        holder.txtRating.setText(String.format("%.1f", movie.getRating()));
        holder.txtQuality.setText(movie.getQuality());
        holder.txtGenre.setText(movie.getGenre());
        holder.txtDuration.setText(movie.getDuration() + " • " + movie.getYear());

        Glide.with(context)
                .load(movie.getBackdropUrl())
                .placeholder(R.drawable.bg_badge_dark)
                .error(R.drawable.bg_badge_dark)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgBackdrop);

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.btnPlay.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBackdrop;
        TextView txtTitle;
        TextView txtRating;
        TextView txtQuality;
        TextView txtGenre;
        TextView txtDuration;
        View btnPlay;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBackdrop = itemView.findViewById(R.id.img_banner_backdrop);
            txtTitle = itemView.findViewById(R.id.txt_banner_title);
            txtRating = itemView.findViewById(R.id.txt_banner_rating);
            txtQuality = itemView.findViewById(R.id.txt_banner_quality);
            txtGenre = itemView.findViewById(R.id.txt_banner_genre);
            txtDuration = itemView.findViewById(R.id.txt_banner_duration);
            btnPlay = itemView.findViewById(R.id.btn_banner_play);
        }
    }
}