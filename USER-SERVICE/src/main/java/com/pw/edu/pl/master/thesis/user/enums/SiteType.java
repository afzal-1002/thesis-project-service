package com.pw.edu.pl.master.thesis.user.enums;

public enum SiteType {

    JIRA_CLOUD("https://%s.atlassian.net"),
    SELF_HOSTED("https://%s"),
    OTHER("%s");

    private final String urlTemplate;

    SiteType(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public String buildBaseUrl(String hostPart) {
        return String.format(urlTemplate, hostPart);
    }
}