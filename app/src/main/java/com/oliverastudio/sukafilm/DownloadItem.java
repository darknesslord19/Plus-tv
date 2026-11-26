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

public class DownloadItem implements Serializable {
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_PAUSED = 4;
    public static final int STATUS_SUCCESSFUL = 8;
    public static final int STATUS_FAILED = 16;

    private long downloadId;
    private int movieId;
    private String movieTitle;
    private String quality;
    private String posterUrl;
    private String downloadUrl;
    private String fileName;
    private long totalBytes;
    private long downloadedBytes;
    private int status;
    private long timestamp;

    public DownloadItem(long downloadId, int movieId, String movieTitle, String quality,
                        String posterUrl, String downloadUrl, String fileName,
                        long totalBytes, long downloadedBytes, int status, long timestamp) {
        this.downloadId = downloadId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.quality = quality;
        this.posterUrl = posterUrl;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.totalBytes = totalBytes;
        this.downloadedBytes = downloadedBytes;
        this.status = status;
        this.timestamp = timestamp;
    }

    public long getDownloadId() { return downloadId; }
    public void setDownloadId(long downloadId) { this.downloadId = downloadId; }

    public int getMovieId() { return movieId; }
    public String getMovieTitle() { return movieTitle; }
    public String getQuality() { return quality; }
    public String getPosterUrl() { return posterUrl; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getFileName() { return fileName; }

    public long getTotalBytes() { return totalBytes; }
    public void setTotalBytes(long totalBytes) { this.totalBytes = totalBytes; }

    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) { this.downloadedBytes = downloadedBytes; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getTimestamp() { return timestamp; }

    public int getProgressPercentage() {
        if (totalBytes <= 0) return 0;
        return (int) ((downloadedBytes * 100) / totalBytes);
    }
}