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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    public interface OnDownloadActionListener {
        void onOpenDownload(DownloadItem item);
        void onDeleteDownload(DownloadItem item);
    }

    private final Context context;
    private List<DownloadItem> downloadList;
    private final OnDownloadActionListener listener;

    public DownloadAdapter(Context context, List<DownloadItem> downloadList, OnDownloadActionListener listener) {
        this.context = context;
        this.downloadList = downloadList != null ? downloadList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<DownloadItem> newList) {
        this.downloadList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        DownloadItem item = downloadList.get(position);

        holder.txtTitle.setText(item.getMovieTitle());
        holder.txtQuality.setText(item.getQuality());
        holder.txtFilename.setText(item.getFileName());

        Glide.with(context)
                .load(item.getPosterUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgPoster);

        int progress = item.getProgressPercentage();
        holder.progressBar.setProgress(progress);

        if (item.getStatus() == DownloadItem.STATUS_SUCCESSFUL || progress >= 100) {
            holder.progressBar.setProgress(100);
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF22C55E));
            holder.txtStatusLabel.setText("Selesai Diunduh");
            holder.txtStatusLabel.setTextColor(0xFF22C55E);
            holder.txtBytes.setText(DownloadManagerHelper.formatBytes(item.getTotalBytes()));
            holder.imgActionIcon.setImageResource(R.drawable.ic_check);
        } else if (item.getStatus() == DownloadItem.STATUS_FAILED) {
            holder.txtStatusLabel.setText("Gagal Mengunduh");
            holder.txtStatusLabel.setTextColor(0xFFEF4444);
            holder.txtBytes.setText(DownloadManagerHelper.formatBytes(item.getDownloadedBytes()) + " / " + DownloadManagerHelper.formatBytes(item.getTotalBytes()));
            holder.imgActionIcon.setImageResource(R.drawable.ic_close);
        } else {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFF5C518));
            holder.txtStatusLabel.setText("Mengunduh (" + progress + "%)");
            holder.txtStatusLabel.setTextColor(0xFFF5C518);
            holder.txtBytes.setText(DownloadManagerHelper.formatBytes(item.getDownloadedBytes()) + " / " + DownloadManagerHelper.formatBytes(item.getTotalBytes()));
            holder.imgActionIcon.setImageResource(R.drawable.ic_close);
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                if (item.getStatus() == DownloadItem.STATUS_SUCCESSFUL) {
                    listener.onOpenDownload(item);
                } else {
                    listener.onDeleteDownload(item);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.getStatus() == DownloadItem.STATUS_SUCCESSFUL) {
                listener.onOpenDownload(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloadList.size();
    }

    static class DownloadViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle;
        TextView txtQuality;
        TextView txtFilename;
        ProgressBar progressBar;
        TextView txtStatusLabel;
        TextView txtBytes;
        FrameLayout btnAction;
        ImageView imgActionIcon;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_download_poster);
            txtTitle = itemView.findViewById(R.id.txt_download_title);
            txtQuality = itemView.findViewById(R.id.txt_download_quality);
            txtFilename = itemView.findViewById(R.id.txt_download_filename);
            progressBar = itemView.findViewById(R.id.prog_download_status);
            txtStatusLabel = itemView.findViewById(R.id.txt_download_status_label);
            txtBytes = itemView.findViewById(R.id.txt_download_bytes);
            btnAction = itemView.findViewById(R.id.btn_download_action);
            imgActionIcon = itemView.findViewById(R.id.img_download_action_icon);
        }
    }
}