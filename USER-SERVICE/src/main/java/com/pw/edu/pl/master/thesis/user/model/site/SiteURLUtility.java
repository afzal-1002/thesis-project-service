package com.pw.edu.pl.master.thesis.user.model.site;



public final class SiteURLUtility {

    private static final String JIRA_CLOUD_ROOT = ".atlassian.net";
    public static String buildURLFromHostPart(String hostPart) {
        if (hostPart == null || hostPart.isBlank()) {
            throw new IllegalArgumentException("hostPart is required");
        }
        String h = hostPart.trim().toLowerCase();

        if (h.startsWith("http://") || h.startsWith("https://")) {
            return normalizeURL(h);
        }

        if (!h.contains(".")) { h = h + JIRA_CLOUD_ROOT; }

        // If host (no scheme) was given, prefix https://
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "https://" + h;
        }
        return normalizeURL(h);
    }

    public static String normalizeURL(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("baseUrl is required");
        }
        String u = url.trim();

        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }

        try {
            var uri = java.net.URI.create(u);
            String host = (uri.getHost() == null ? "" : uri.getHost().toLowerCase());
            if (host.isBlank()) throw new IllegalArgumentException("Invalid URL: " + url);
            return "https://" + host; // canonical form
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}
