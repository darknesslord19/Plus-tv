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

public class Season implements Serializable {
    private String seasonId;
    private String seasonTitle;
    private List<Episode> episodes;

    public Season(String seasonId, String seasonTitle, List<Episode> episodes) {
        this.seasonId = seasonId;
        this.seasonTitle = seasonTitle;
        this.episodes = episodes != null ? episodes : new ArrayList<>();
    }

    public String getSeasonId() { return seasonId; }
    public String getSeasonTitle() { return seasonTitle; }
    public List<Episode> getEpisodes() { return episodes; }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes != null ? episodes : new ArrayList<>();
    }
}