package com.pw.edu.pl.master.thesis.issues.configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Configuration
public class JiraWebClientConfig {

    private final ObjectProvider<RequestCredentials> credentialsProvider;

    public JiraWebClientConfig(ObjectProvider<RequestCredentials> credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    @Bean
    public WebClient jiraWebClient() {
        return WebClient.builder()
                .filter((request, next) -> {
                    // get current request-scoped creds
                    var creds = credentialsProvider.getObject();

                    URI original = request.url();
                    boolean isAbsolute = original.isAbsolute();

                    URI finalUri;
                    if (isAbsolute) {
                        // e.g. you passed: https://bugresolution.atlassian.net/rest/api/3/issue/BUG-5?fields=attachment
                        // → KEEP IT
                        finalUri = original;
                    } else {
                        // e.g. you passed: /rest/api/3/issue/BUG-5?fields=attachment
                        // → prepend user's baseUrl but KEEP query
                        String path = original.getRawPath();   // /rest/api/3/issue/BUG-5
                        String query = original.getRawQuery(); // fields=attachment

                        StringBuilder sb = new StringBuilder();
                        sb.append(creds.baseUrl());
                        if (path != null) sb.append(path);
                        if (query != null && !query.isBlank()) {
                            sb.append('?').append(query);
                        }
                        finalUri = URI.create(sb.toString());
                    }

                    ClientRequest mutated = ClientRequest
                            .from(request)
                            .url(finalUri)
                            .headers(h -> h.setBasicAuth(creds.username(), creds.token()))
                            .build();

                    return next.exchange(mutated);
                })
                .build();
    }
}
