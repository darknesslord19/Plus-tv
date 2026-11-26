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

public class Episode implements Serializable {
    private String episodeId;
    private String title;
    private String duration;
    private String thumbnail;
    private String downloadUrl;
    private String fileSize;
    private String quality;

    public Episode(String episodeId, String title, String duration, String thumbnail,
                   String downloadUrl, String fileSize, String quality) {
        this.episodeId = episodeId;
        this.title = title;
        this.duration = duration;
        this.thumbnail = thumbnail;
        this.downloadUrl = downloadUrl;
        this.fileSize = fileSize;
        this.quality = quality;
    }

    public String getEpisodeId() { return episodeId; }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public String getThumbnail() { return thumbnail; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getFileSize() { return fileSize; }
    public String getQuality() { return quality; }
}