package com.oliverastudio.sukafilm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Minimal extended-M3U parser (no Android dependencies). */
public final class M3uParser {
    public static final String DEFAULT_GROUP = "Lainnya";
    private static final Pattern ATTRIBUTE = Pattern.compile("([A-Za-z0-9_-]+)=\"([^\"]*)\"");

    public static final class Entry {
        public String title;
        public String url;
        public String group = DEFAULT_GROUP;
        public final Map<String, String> attributes = new HashMap<>();

        public String attribute(String key, String fallback) {
            String value = attributes.get(key);
            return value == null || value.trim().isEmpty() ? fallback : value.trim();
        }
    }

    private M3uParser() {}

    public static List<Entry> parse(String text) {
        List<Entry> result = new ArrayList<>();
        Entry pending = null;
        String extGroup = null;
        for (String raw : text.replace("﻿", "").split("\\r?\\n|\\r")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#EXTINF:")) {
                pending = parseInfo(line);
                extGroup = null;
            } else if (line.startsWith("#EXTGRP:")) {
                extGroup = line.substring(8).trim();
            } else if (!line.startsWith("#")) {
                String lower = line.toLowerCase(Locale.ROOT);
                if (pending != null && (lower.startsWith("https://") || lower.startsWith("http://"))) {
                    pending.url = line;
                    if (DEFAULT_GROUP.equals(pending.group) && extGroup != null && !extGroup.isEmpty()) {
                        pending.group = extGroup;
                    }
                    result.add(pending);
                }
                pending = null;
                extGroup = null;
            }
        }
        return result;
    }

    private static Entry parseInfo(String line) {
        // The title starts after the first comma that is not inside quotes.
        int comma = -1;
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') quoted = !quoted;
            else if (c == ',' && !quoted) {
                comma = i;
                break;
            }
        }
        Entry entry = new Entry();
        Matcher matcher = ATTRIBUTE.matcher(comma < 0 ? line : line.substring(0, comma));
        while (matcher.find()) {
            entry.attributes.put(matcher.group(1).toLowerCase(Locale.ROOT), matcher.group(2));
        }
        String title = comma < 0 ? "" : line.substring(comma + 1).trim();
        entry.title = title.isEmpty() ? entry.attribute("tvg-name", "Tanpa judul") : title;
        entry.group = entry.attribute("group-title", DEFAULT_GROUP);
        return entry;
    }
}
