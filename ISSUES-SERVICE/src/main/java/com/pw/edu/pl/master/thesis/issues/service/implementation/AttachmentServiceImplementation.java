package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.pw.edu.pl.master.thesis.issues.configuration.RequestCredentials;
import com.pw.edu.pl.master.thesis.issues.dto.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.issues.dto.issue.attachment.DownloadedFile;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Attachment;
import com.pw.edu.pl.master.thesis.issues.exception.UserNotAuthorizedException;
import com.pw.edu.pl.master.thesis.issues.service.AttachmentService;
import com.pw.edu.pl.master.thesis.issues.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImplementation implements AttachmentService {

    private final HelperMethod helperMethod;
    private final RequestCredentials requestCredentials;
    private final JiraIssueService jiraIssueService;

    // both clients injected
    private final WebClient jiraWebClient;
    private final RestTemplate restTemplate;

    /* ==========================================================
       1. WEBCLIENT versions
       ========================================================== */

    @Override
    public DownloadedFile downloadFirstAttachmentWithWeb(String issueKey) {
        var attachments = findAttachments(issueKey);
        var first = attachments.getFirst();
        return downloadFromUrlWithWeb(first.getContent());
    }

    @Override
    public List<DownloadedFile> downloadAllAttachmentsWithWeb(String issueKey) {
        var attachments = findAttachments(issueKey);
        List<DownloadedFile> result = new ArrayList<>();
        for (var att : attachments) {
            result.add(downloadFromUrlWithWeb(att.getContent()));
        }
        return result;
    }

    /* ==========================================================
       2. RESTTEMPLATE versions
       ========================================================== */

    @Override
    public DownloadedFile downloadFirstAttachmentWithRest(String issueKey) {
        var attachments = findAttachments(issueKey);
        var first = attachments.getFirst();
        return downloadFromUrlWithRest(first.getContent());
    }

    @Override
    public List<DownloadedFile> downloadAllAttachmentsWithRest(String issueKey) {
        var attachments = findAttachments(issueKey);
        List<DownloadedFile> result = new ArrayList<>();
        for (var att : attachments) {
            result.add(downloadFromUrlWithRest(att.getContent()));
        }
        return result;
    }

    /* ==========================================================
       COMMON HELPERS
       ========================================================== */

    // this is your Jira attachment DTO path – adjust if your package is different
    private List<Attachment> findAttachments(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        var issue = jiraIssueService.getIssueWithSelectedFields(issueKey, "attachment");
        var attachments = issue.getFields().getAttachment();
        if (attachments == null || attachments.isEmpty()) {
            throw new IllegalStateException("No attachments on issue " + issueKey);
        }
        return attachments;
    }

    /* ==========================================================
       WEBCLIENT download
       ========================================================== */

    private DownloadedFile downloadFromUrlWithWeb(String contentUrl) {
        log.info("Downloading attachment (WebClient) from Jira: {}", contentUrl);

        try {
            ClientResponse firstResp = jiraWebClient
                    .get()
                    .uri(contentUrl)
                    .headers(h -> {
                        h.setBasicAuth(requestCredentials.username(), requestCredentials.token(), StandardCharsets.UTF_8);
                        h.set(HttpHeaders.ACCEPT, "*/*");
                    })
                    .exchangeToMono(Mono::just)
                    .block();

            if (firstResp == null) {
                throw new IllegalStateException("Empty response while downloading attachment from " + contentUrl);
            }

            HttpStatus status = (HttpStatus) firstResp.statusCode();

            // 3xx → follow redirect
            if (status.is3xxRedirection()) {
                String redirect = firstResp.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION);
                if (redirect == null || redirect.isBlank()) {
                    throw new IllegalStateException("Jira returned redirect but no Location header for " + contentUrl);
                }
                log.info("Attachment redirect (WebClient) → {}", redirect);

                byte[] bytes = jiraWebClient
                        .get()
                        .uri(redirect)
                        .headers(h -> h.set(HttpHeaders.ACCEPT, "*/*"))
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();

                if (bytes == null) {
                    throw new IllegalStateException("Empty response after redirect while downloading attachment from " + redirect);
                }

                String filename = extractFilename(null, contentUrl);
                return new DownloadedFile(bytes, MediaType.APPLICATION_OCTET_STREAM_VALUE, filename, bytes.length);
            }

            // 200 OK
            if (status.is2xxSuccessful()) {
                byte[] bytes = firstResp.bodyToMono(byte[].class).block();
                if (bytes == null) {
                    throw new IllegalStateException("Empty body while downloading attachment from " + contentUrl);
                }

                MediaType ct = firstResp.headers()
                        .contentType()
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);

                long length = firstResp.headers()
                        .contentLength()
                        .orElse((long) bytes.length);

                ContentDisposition cd = firstResp.headers().asHttpHeaders().getContentDisposition();
                String filename = extractFilename(cd, contentUrl);

                return new DownloadedFile(bytes, ct.toString(), filename, length);
            }

            // other → error
            String body = firstResp.bodyToMono(String.class).block();
            throw new IllegalStateException("Failed to download attachment (WebClient) from " + contentUrl +
                    " → " + status.value() + " " + status.getReasonPhrase() +
                    " → " + body);

        } catch (WebClientResponseException e) {
            throw new UserNotAuthorizedException(e.getResponseBodyAsString());
        }
    }

    /* ==========================================================
       RESTTEMPLATE download
       ========================================================== */

    private DownloadedFile downloadFromUrlWithRest(String contentUrl) {
        log.info("Downloading attachment (RestTemplate) from Jira: {}", contentUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "*/*");
        headers.setBasicAuth(
                requestCredentials.username(),
                requestCredentials.token(),
                StandardCharsets.UTF_8
        );
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(
                    contentUrl,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            // 3xx → follow redirect manually (extra safety)
            if (resp.getStatusCode().is3xxRedirection()) {
                String redirect = resp.getHeaders().getLocation() != null
                        ? resp.getHeaders().getLocation().toString()
                        : null;
                if (redirect == null || redirect.isBlank()) {
                    throw new IllegalStateException("Jira returned redirect but no Location header for " + contentUrl);
                }
                log.info("Attachment redirect (RestTemplate) → {}", redirect);

                HttpHeaders h2 = new HttpHeaders();
                h2.set(HttpHeaders.ACCEPT, "*/*");
                ResponseEntity<byte[]> resp2 = restTemplate.exchange(
                        redirect,
                        HttpMethod.GET,
                        new HttpEntity<>(h2),
                        byte[].class
                );

                if (!resp2.getStatusCode().is2xxSuccessful() || resp2.getBody() == null) {
                    throw new IllegalStateException("Empty response after redirect while downloading attachment from " + redirect);
                }

                String filename = extractFilename(null, contentUrl);
                long length = resp2.getHeaders().getContentLength();
                if (length < 0) length = resp2.getBody().length;

                return new DownloadedFile(
                        resp2.getBody(),
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        filename,
                        length
                );
            }

            // 200 OK
            if (resp.getStatusCode().is2xxSuccessful()) {
                byte[] bytes = resp.getBody();
                if (bytes == null) {
                    throw new IllegalStateException("Empty body while downloading attachment from " + contentUrl);
                }

                MediaType ct = resp.getHeaders().getContentType();
                if (ct == null) ct = MediaType.APPLICATION_OCTET_STREAM;

                long length = resp.getHeaders().getContentLength();
                if (length < 0) length = bytes.length;

                ContentDisposition cd = resp.getHeaders().getContentDisposition();
                String filename = extractFilename(cd, contentUrl);

                return new DownloadedFile(bytes, ct.toString(), filename, length);
            }

            throw new IllegalStateException("Failed to download attachment (RestTemplate) from " + contentUrl +
                    " → " + resp.getStatusCode().value() + " " );

        } catch (HttpClientErrorException e) {
            throw new UserNotAuthorizedException(e.getResponseBodyAsString());
        }
    }

    /* ==========================================================
       filename helper
       ========================================================== */

    private static String extractFilename(ContentDisposition cd, String fallbackUrl) {
        if (cd != null && cd.getFilename() != null && !cd.getFilename().isBlank()) {
            return cd.getFilename();
        }
        try {
            String path = new URI(fallbackUrl).getPath();
            int i = path.lastIndexOf('/');
            return (i >= 0 && i < path.length() - 1) ? path.substring(i + 1) : "attachment";
        } catch (Exception ignore) {
            return "attachment";
        }
    }




    @Override
    public DownloadedFile downloadAttachment(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        // 1) Get the issue (ensure your IssueResponse exposes fields.attachments[].content)
        var issue = jiraIssueService.getIssueByIdOrKey(issueKey);
        var attachments = issue.getFields().getAttachment(); // adjust to your DTO path
        if (attachments == null || attachments.isEmpty()) {
            throw new IllegalStateException("No attachments on issue " + issueKey);
        }

        // 2) Take the first attachment content URL
        String contentUrl = attachments.getFirst().getContent(); // Jira “content” is the binary URL

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        headers.setBasicAuth(requestCredentials.username(), requestCredentials.token(), StandardCharsets.UTF_8);

        HttpEntity<Void> req = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(contentUrl, HttpMethod.GET, req, byte[].class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Attachment download failed: " + resp.getStatusCode());
            }

            MediaType ct = resp.getHeaders().getContentType();
            String mime = (ct != null) ? ct.toString() : "application/octet-stream";
            long length = resp.getHeaders().getContentLength();

            ContentDisposition cd = resp.getHeaders().getContentDisposition();
            String filename = extractFilename(cd, contentUrl);

            byte[] bytes = resp.getBody();
            if (bytes == null) bytes = new byte[0];

            return new DownloadedFile(bytes, mime, filename, length);
        } catch (HttpStatusCodeException e) {
            throw new UserNotAuthorizedException(e.getResponseBodyAsString());
        }
    }

    @Override
    public List<DownloadedFile> downloadAllAttachments(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        // 1) get issue from Jira
        var issue = jiraIssueService.getIssueByIdOrKey(issueKey);

        // 2) read attachments array
        var fields = issue.getFields();
        if (fields == null || fields.getAttachment() == null || fields.getAttachment().isEmpty()) {
            return List.of();
        }

        List<DownloadedFile> out = new java.util.ArrayList<>();

        for (var att : fields.getAttachment()) {
            // Jira attachment JSON usually has "content" or "contentUrl"
            String contentUrl = att.getContent(); // adjust to your DTO field name
            if (contentUrl == null || contentUrl.isBlank()) continue;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.ALL));
            headers.setBasicAuth(requestCredentials.username(),
                    requestCredentials.token(),
                    StandardCharsets.UTF_8);

            HttpEntity<Void> req = new HttpEntity<>(headers);

            try {
                ResponseEntity<byte[]> resp = restTemplate.exchange(contentUrl,
                        HttpMethod.GET, req, byte[].class);

                if (!resp.getStatusCode().is2xxSuccessful()) {
                    log.warn("Attachment download failed for {}: {}", contentUrl, resp.getStatusCode());
                    continue;
                }

                MediaType ct = resp.getHeaders().getContentType();
                String mime = (ct != null) ? ct.toString() : "application/octet-stream";
                long length = resp.getHeaders().getContentLength();

                ContentDisposition cd = resp.getHeaders().getContentDisposition();
                String filename = cd.getFilename() != null && !cd.getFilename().isBlank()
                        ? cd.getFilename()
                        : safeFileName(att.getFilename(), contentUrl);

                byte[] bytes = resp.getBody();
                if (bytes == null) bytes = new byte[0];

                out.add(new DownloadedFile(bytes, mime, filename, length));

            } catch (HttpStatusCodeException e) {
                log.warn("Error downloading attachment {}: {}", contentUrl, e.getStatusCode());
            }
        }

        return out;
    }

    private String safeFileName(String attFilename, String fallbackUrl) {
        if (attFilename != null && !attFilename.isBlank()) {
            return attFilename;
        }
        try {
            String path = new java.net.URI(fallbackUrl).getPath();
            int i = path.lastIndexOf('/');
            return (i >= 0 && i < path.length() - 1) ? path.substring(i + 1) : "attachment";
        } catch (Exception ignore) {
            return "attachment";
        }
    }


}
