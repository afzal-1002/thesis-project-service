package com.pw.edu.pl.master.thesis.user.model.helper;


import com.pw.edu.pl.master.thesis.user.enums.JiraApiEndpoint;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class JiraUrlBuilder {

    @Value("${jira.api-version:3}")
    private String apiVersion;

    private static final String ATTR_HOST_URL = "hostUrl";
    private final HttpSession session;

    public String url(String baseUrl, JiraApiEndpoint endpoint) {
        if (endpoint == null) throw new IllegalArgumentException("Jira endpoint cannot be null");
        if (baseUrl == null || baseUrl.isBlank()) throw new IllegalArgumentException("Base URL cannot be empty");
        String normalized = normalizeJiraBaseUrl(baseUrl); // returns full https://... base
        return normalized + "/rest/api/" + apiVersion + endpoint.getPath();
    }

    /** Build URL using base URL stored in session under 'hostUrl'. */
    public String url(JiraApiEndpoint endpoint) {
        if (endpoint == null) throw new IllegalArgumentException("Jira endpoint cannot be null");
        Object hostAttr = session.getAttribute(ATTR_HOST_URL);
        if (hostAttr == null) throw new IllegalStateException("No Jira baseUrl in session. Please log in first.");

        String base = hostAttr.toString(); // now this is full https://... (see normalizeJiraBaseUrl)
        // safety: if someone mistakenly saved only the host, repair it
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "https://" + base;
        }
        return base + "/rest/api/" + apiVersion + endpoint.getPath();
    }

    public String getSessionBaseUrl() {
        Object hostAttr = session.getAttribute(ATTR_HOST_URL);
        if (hostAttr == null) throw new IllegalStateException("No Jira baseUrl in session. Please log in first.");
        return hostAttr.toString(); // full https://... base
    }

    /** Always returns a full https://<tenant>.atlassian.net and stores that in session. */
    public String normalizeJiraBaseUrl(String url) {
        String baseUrl = url == null ? "" : url.trim();
        if (baseUrl.isEmpty()) throw new IllegalArgumentException("jiraUrl is required");

        // Accept short forms like "bugresolution" and turn into full cloud URL
        if (!baseUrl.contains("://") && !baseUrl.contains(".")) {
            baseUrl = "https://" + baseUrl.toLowerCase() + ".atlassian.net";
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        URI uri = URI.create(baseUrl);
        String host = uri.getHost();
        if (host == null || !host.endsWith(".atlassian.net")) {
            throw new IllegalArgumentException("Invalid Jira Cloud host: " + baseUrl);
        }

        String normalized = "https://" + host;   // <- FULL base with scheme
        session.setAttribute(ATTR_HOST_URL, normalized);
        return normalized;
    }
}
