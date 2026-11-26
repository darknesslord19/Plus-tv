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

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManagerHelper {
    private static final String PREF_NAME = "sukafilm_downloads_pref";
    private static final String KEY_DOWNLOADS = "downloads_list";
    public static final String FOLDER_NAME = "Suka Film";

    private final Context context;
    private final DownloadManager downloadManager;
    private final SharedPreferences prefs;

    public DownloadManagerHelper(Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public long startDownload(Movie movie, String quality, String url, String targetSize) {
        String cleanTitle = movie.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_");
        String fileName = "SukaFilm_" + cleanTitle + "_" + quality.replace(" ", "_") + ".mp4";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Suka Film: " + movie.getTitle() + " (" + quality + ")");
        request.setDescription("Menyimpan ke folder /" + FOLDER_NAME + "/");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String subPath = FOLDER_NAME + "/" + fileName;
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER_NAME);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath);
        } catch (Exception e) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        }

        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);

        long downloadId = -1;
        try {
            downloadId = downloadManager.enqueue(request);
        } catch (Exception e) {
            Log.e("DownloadManagerHelper", "Enqueue error: " + e.getMessage());
            downloadId = System.currentTimeMillis();
        }

        long approxBytes = parseApproxSizeToBytes(targetSize);
        DownloadItem item = new DownloadItem(
                downloadId,
                movie.getId(),
                movie.getTitle(),
                quality,
                movie.getPosterUrl(),
                url,
                fileName,
                approxBytes,
                0,
                DownloadItem.STATUS_RUNNING,
                System.currentTimeMillis()
        );
        saveDownloadItem(item);
        return downloadId;
    }

    public List<DownloadItem> getAllDownloads() {
        List<DownloadItem> list = new ArrayList<>();
        String json = prefs.getString(KEY_DOWNLOADS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                DownloadItem item = new DownloadItem(
                        obj.optLong("downloadId"),
                        obj.optInt("movieId"),
                        obj.optString("movieTitle"),
                        obj.optString("quality"),
                        obj.optString("posterUrl"),
                        obj.optString("downloadUrl"),
                        obj.optString("fileName"),
                        obj.optLong("totalBytes"),
                        obj.optLong("downloadedBytes"),
                        obj.optInt("status"),
                        obj.optLong("timestamp")
                );
                updateItemLiveStatus(item);
                list.add(item);
            }
        } catch (Exception e) {
            Log.e("DownloadManagerHelper", "Error loading downloads: " + e.getMessage());
        }
        return list;
    }

    private void updateItemLiveStatus(DownloadItem item) {
        if (downloadManager == null) return;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(item.getDownloadId());
        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                if (statusIndex != -1) {
                    int dmStatus = cursor.getInt(statusIndex);
                    if (dmStatus == DownloadManager.STATUS_SUCCESSFUL) {
                        item.setStatus(DownloadItem.STATUS_SUCCESSFUL);
                    } else if (dmStatus == DownloadManager.STATUS_FAILED) {
                        item.setStatus(DownloadItem.STATUS_FAILED);
                    } else if (dmStatus == DownloadManager.STATUS_RUNNING) {
                        item.setStatus(DownloadItem.STATUS_RUNNING);
                    } else if (dmStatus == DownloadManager.STATUS_PAUSED) {
                        item.setStatus(DownloadItem.STATUS_PAUSED);
                    } else {
                        item.setStatus(DownloadItem.STATUS_PENDING);
                    }
                }

                if (bytesDownloadedIndex != -1 && totalBytesIndex != -1) {
                    long downloaded = cursor.getLong(bytesDownloadedIndex);
                    long total = cursor.getLong(totalBytesIndex);
                    if (downloaded > 0) item.setDownloadedBytes(downloaded);
                    if (total > 0) item.setTotalBytes(total);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public synchronized void saveDownloadItem(DownloadItem item) {
        List<DownloadItem> currentList = getAllDownloads();
        boolean exists = false;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getDownloadId() == item.getDownloadId()) {
                currentList.set(i, item);
                exists = true;
                break;
            }
        }
        if (!exists) {
            currentList.add(0, item);
        }
        persistList(currentList);
    }

    public synchronized void deleteDownload(long downloadId) {
        try {
            if (downloadManager != null) {
                downloadManager.remove(downloadId);
            }
        } catch (Exception ignored) {}

        List<DownloadItem> currentList = getAllDownloads();
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getDownloadId() == downloadId) {
                currentList.remove(i);
                break;
            }
        }
        persistList(currentList);
    }

    public synchronized void clearAllDownloads() {
        List<DownloadItem> currentList = getAllDownloads();
        for (DownloadItem item : currentList) {
            try {
                if (downloadManager != null) {
                    downloadManager.remove(item.getDownloadId());
                }
            } catch (Exception ignored) {}
        }
        prefs.edit().remove(KEY_DOWNLOADS).apply();
    }

    private void persistList(List<DownloadItem> list) {
        JSONArray array = new JSONArray();
        for (DownloadItem item : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("downloadId", item.getDownloadId());
                obj.put("movieId", item.getMovieId());
                obj.put("movieTitle", item.getMovieTitle());
                obj.put("quality", item.getQuality());
                obj.put("posterUrl", item.getPosterUrl());
                obj.put("downloadUrl", item.getDownloadUrl());
                obj.put("fileName", item.getFileName());
                obj.put("totalBytes", item.getTotalBytes());
                obj.put("downloadedBytes", item.getDownloadedBytes());
                obj.put("status", item.getStatus());
                obj.put("timestamp", item.getTimestamp());
                array.put(obj);
            } catch (Exception ignored) {}
        }
        prefs.edit().putString(KEY_DOWNLOADS, array.toString()).apply();
    }

    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 MB";
        double kb = bytes / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        if (gb >= 1.0) {
            return String.format("%.1f GB", gb);
        } else {
            return String.format("%.0f MB", mb);
        }
    }

    private long parseApproxSizeToBytes(String sizeStr) {
        if (sizeStr == null) return 1024 * 1024 * 500;
        try {
            if (sizeStr.contains("GB")) {
                double val = Double.parseDouble(sizeStr.replace("GB", "").trim());
                return (long) (val * 1024 * 1024 * 1024);
            } else if (sizeStr.contains("MB")) {
                double val = Double.parseDouble(sizeStr.replace("MB", "").trim());
                return (long) (val * 1024 * 1024);
            }
        } catch (Exception ignored) {}
        return 1024 * 1024 * 800;
    }

    public String getDownloadFolderPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + FOLDER_NAME;
    }

    public void setCustomDownloadFolder(String path) {
    }

    /** content:// URI that other apps may open (file:// is blocked since Android 7). */
    public Uri getDownloadedUri(DownloadItem item) {
        return downloadManager.getUriForDownloadedFile(item.getDownloadId());
    }

    public File getDownloadedFile(DownloadItem item) {
        if (item == null || item.getFileName() == null) return null;
        File fileInFolder = new File(getDownloadFolderPath(), item.getFileName());
        if (fileInFolder.exists()) return fileInFolder;
        File defaultFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER_NAME + "/" + item.getFileName());
        if (defaultFile.exists()) return defaultFile;
        File rootDownload = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), item.getFileName());
        if (rootDownload.exists()) return rootDownload;
        return fileInFolder;
    }
}