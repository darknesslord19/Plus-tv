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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    public interface OnEpisodeActionListener {
        void onPlayEpisode(Episode episode);
        void onDownloadEpisode(Episode episode);
    }

    private final Context context;
    private List<Episode> episodeList;
    private final OnEpisodeActionListener listener;

    public EpisodeAdapter(Context context, List<Episode> episodeList, OnEpisodeActionListener listener) {
        this.context = context;
        this.episodeList = episodeList != null ? episodeList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateEpisodes(List<Episode> newEpisodes) {
        this.episodeList = newEpisodes != null ? newEpisodes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode_card, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode episode = episodeList.get(position);

        holder.txtTitle.setText(episode.getTitle());
        holder.txtDuration.setText(episode.getDuration());
        holder.txtInfo.setText(episode.getQuality() + " • " + episode.getFileSize());

        Glide.with(context)
                .load(episode.getThumbnail())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayEpisode(episode);
            }
        });

        holder.btnDownload.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadEpisode(episode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView txtDuration;
        TextView txtTitle;
        TextView txtInfo;
        FrameLayout btnDownload;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_episode_thumb);
            txtDuration = itemView.findViewById(R.id.txt_episode_duration);
            txtTitle = itemView.findViewById(R.id.txt_episode_title);
            txtInfo = itemView.findViewById(R.id.txt_episode_info);
            btnDownload = itemView.findViewById(R.id.btn_episode_download);
        }
    }
}