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

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class DownloadsActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private TextView btnClearAll;
    private TextView txtHeaderSummary;
    private LinearLayout layoutEmpty;
    private Button btnExplore;
    private RecyclerView recyclerDownloads;

    private DownloadManagerHelper downloadHelper;
    private DownloadAdapter adapter;
    private final Handler progressPollHandler = new Handler(Looper.getMainLooper());
    private Runnable progressPollRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads);

        downloadHelper = new DownloadManagerHelper(this);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupProgressPolling();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_downloads_back);
        btnClearAll = findViewById(R.id.btn_clear_all_downloads);
        txtHeaderSummary = findViewById(R.id.txt_downloads_header_summary);
        layoutEmpty = findViewById(R.id.layout_empty_downloads);
        btnExplore = findViewById(R.id.btn_explore_movies_now);
        recyclerDownloads = findViewById(R.id.recycler_downloads);
    }

    private void setupRecyclerView() {
        recyclerDownloads.setLayoutManager(new LinearLayoutManager(this));
        List<DownloadItem> items = downloadHelper.getAllDownloads();

        adapter = new DownloadAdapter(this, items, new DownloadAdapter.OnDownloadActionListener() {
            @Override
            public void onOpenDownload(DownloadItem item) {
                openDownloadedFile(item);
            }

            @Override
            public void onDeleteDownload(DownloadItem item) {
                confirmDeleteDownload(item);
            }
        });

        recyclerDownloads.setAdapter(adapter);
        updateUIState(items);
    }

    private void updateUIState(List<DownloadItem> items) {
        if (items.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerDownloads.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
            txtHeaderSummary.setText("Henüz indirilen dosya yok.");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerDownloads.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
            txtHeaderSummary.setText("Toplam: " + items.size() + " video • /Suka Film/ klasöründe");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnExplore.setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("İndirme geçmişini sil")
                    .setMessage("Tüm indirmeler silinsin mi?")
                    .setPositiveButton("Sil", (dialog, which) -> {
                        downloadHelper.clearAllDownloads();
                        refreshDownloadsList();
                        Toast.makeText(this, "İndirmeler temizlendi", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("İptal", null)
                    .show();
        });
    }

    private void setupProgressPolling() {
        progressPollRunnable = new Runnable() {
            @Override
            public void run() {
                refreshDownloadsList();
                progressPollHandler.postDelayed(this, 1500);
            }
        };
    }

    private void refreshDownloadsList() {
        List<DownloadItem> items = downloadHelper.getAllDownloads();
        adapter.updateList(items);
        updateUIState(items);
    }

    private void openDownloadedFile(DownloadItem item) {
        File sukaFilmFile = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Suka Film"), item.getFileName());
        File normalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), item.getFileName());
        File file = sukaFilmFile.exists() ? sukaFilmFile : normalFile;
        if (file.exists()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(downloadHelper.getDownloadedUri(item), "video/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }

        Toast.makeText(this, "Önizleme açılıyor: " + item.getMovieTitle(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie_id", item.getMovieId());
        startActivity(intent);
    }

    private void confirmDeleteDownload(DownloadItem item) {
        new AlertDialog.Builder(this)
                .setTitle("İndirmeyi iptal et / sil")
                .setMessage("İndirme silinsin mi: " + item.getMovieTitle() + "?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    downloadHelper.deleteDownload(item.getDownloadId());
                    refreshDownloadsList();
                    Toast.makeText(this, "İndirme silindi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDownloadsList();
        progressPollHandler.postDelayed(progressPollRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressPollHandler.removeCallbacks(progressPollRunnable);
    }
}