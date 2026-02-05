package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.pw.edu.pl.master.thesis.issues.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.issues.configuration.RequestCredentials;
import com.pw.edu.pl.master.thesis.issues.dto.comment.*;
import com.pw.edu.pl.master.thesis.issues.dto.comment.*;
import com.pw.edu.pl.master.thesis.issues.dto.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.issues.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.issues.enums.PushStatus;
import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.mapper.CommentMapper;
import com.pw.edu.pl.master.thesis.issues.model.comment.Comment;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import com.pw.edu.pl.master.thesis.issues.repository.CommentRepository;
import com.pw.edu.pl.master.thesis.issues.repository.IssueRepository;
import com.pw.edu.pl.master.thesis.issues.service.JiraCommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service("jiraCommentServiceImplementation")
@RequiredArgsConstructor
@Slf4j
public class JiraCommentServiceImplementation implements JiraCommentService {

    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder          jiraUrlBuilder;
    private final RequestCredentials      credentials;

    private final CommentRepository commentRepository;
    private final HelperMethod      helperMethod;
    private final CommentMapper     commentMapper;
    private final IssueRepository   issueRepository;

    /** Wrapper for GET /issue/{issueKey}/comment */
    private static class CommentsWrapper { public List<CommentResponse> comments; }

    /* -------------------- helpers -------------------- */

    private static String enc(String raw) { return UriUtils.encodePath(raw, StandardCharsets.UTF_8); }
    private static String seg(String raw) { return UriUtils.encodePathSegment(raw, StandardCharsets.UTF_8); }

    private String commentsBaseUrl(String baseUrl, String issueKey) {
        String template = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.ISSUE_COMMENTS);
        return String.format(template, enc(issueKey));
    }

    private String commentByIdUrl(String baseUrl, String issueKey, String commentId) {
        String template = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.ISSUE_COMMENT_BY_ID);
        return String.format(template, enc(issueKey), enc(commentId));
    }

    /* -------------------- API methods -------------------- */

    @Override
    public CommentResponse createCommentsByIssueKey(String issueKey, CommentRequest request) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        Objects.requireNonNull(request, "request is required");

        String url = commentsBaseUrl(credentials.getBaseUrl(), issueKey);
        return jiraClientConfiguration.post(url, request, CommentResponse.class,
                credentials.getUsername(), credentials.getToken());
    }

    @Override
    public CommentResponse addFullComment(String issueKey, CreateCommentRequest request) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        Objects.requireNonNull(request, "CreateCommentRequest is required");

        String url = commentsBaseUrl(credentials.getBaseUrl(), issueKey);
        return jiraClientConfiguration.post(url, request, CommentResponse.class,
                credentials.getUsername(), credentials.getToken());
    }

    @Override
    public CommentResponse updateCommentByIssueKeyCommentId(String issueKey, String commentId, UpdateCommentRequest request) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        helperMethod.requireNonBlank(commentId, "commentId is required");
        Objects.requireNonNull(request, "UpdateCommentRequest is required");

        String url = commentByIdUrl(credentials.getBaseUrl(), issueKey, commentId);
        return jiraClientConfiguration.put(url, request, CommentResponse.class,
                credentials.getUsername(), credentials.getToken());
    }

    @Override
    public String deleteComment(String issueKey, String commentId) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        helperMethod.requireNonBlank(commentId, "commentId is required");

        String templateUrl = jiraUrlBuilder.url(credentials.getBaseUrl(), JiraApiEndpoint.ISSUE_COMMENT_BY_ID);
        String finalUrl = String.format(templateUrl, seg(issueKey), seg(commentId));
        log.info("DELETE {}", finalUrl);

        try {
            jiraClientConfiguration.delete(finalUrl, Void.class, credentials.getUsername(), credentials.getToken());
            return "Comment deleted successfully with ID: " + commentId;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "No comment found with ID: " + commentId + " (or you do not have permission)";
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "You do not have permission to delete comment with ID: " + commentId;
            }
            throw e;
        }
    }

    @Override
    public List<CommentResponse> getCommentsByIssueKey(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        String url = commentsBaseUrl(credentials.getBaseUrl(), issueKey);
        CommentsWrapper wrapper = jiraClientConfiguration.get(url, CommentsWrapper.class,
                credentials.getUsername(), credentials.getToken());
        return (wrapper == null || wrapper.comments == null) ? List.of() : wrapper.comments;
    }

    @Override
    public List<CommentResponse> getCommentsByIssueKeysList(ListIssueKeys request) {
        List<String> keys = (request != null) ? request.getListIssueKeys() : null;
        if (keys == null || keys.isEmpty()) return List.of();

        List<CommentResponse> all = new ArrayList<>();
        for (String key : keys) { all.addAll(getCommentsByIssueKey(key)); }
        return all;
    }

    @Override
    public CommentResponse getCommentById(String commentId) {
        helperMethod.requireNonBlank(commentId, "commentId is required");

        JiraCommentIdsList payload = JiraCommentIdsList.builder()
                .ids(List.of(commentId))
                .build();

        List<CommentResponse> comments = getCommentByCommentIdsList(payload).getComments();
        return comments.stream()
                .filter(c -> commentId.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CommentListResponse getCommentByCommentIdsList(JiraCommentIdsList commentIdsList) {
        if (commentIdsList == null || commentIdsList.getIds() == null || commentIdsList.getIds().isEmpty()) {
            return CommentListResponse.builder().total(0L).comments(Collections.emptyList()).build();
        }

        List<String> ids = commentIdsList.getIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return CommentListResponse.builder().total(0L).comments(Collections.emptyList()).build();
        }

        log.info("Normalized IDs to fetch: {}", ids);
        String url = jiraUrlBuilder.url(credentials.getBaseUrl(), JiraApiEndpoint.COMMENT_LIST);
        log.info("Target URL: {}", url);

        JiraCommentIdsList requestBody = new JiraCommentIdsList(ids);
        return jiraClientConfiguration.post(url, requestBody, CommentListResponse.class,
                credentials.getUsername(), credentials.getToken());
    }

    @Override
    public CommentListEnvelope getCommentsByIdsWithReport(JiraCommentIdsList payload) {
        CommentListResponse ordered = getCommentByCommentIdsList(payload);

        if (ordered == null || ordered.getComments() == null) {
            return CommentListEnvelope.builder()
                    .comments(Collections.emptyList())
                    .missingIds(payload.getIds())
                    .build();
        }

        Set<String> returnedIds = ordered.getComments().stream()
                .map(CommentResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> missing = payload.getIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(id -> !returnedIds.contains(id))
                .toList();

        return CommentListEnvelope.builder()
                .comments(ordered.getComments())
                .missingIds(missing)
                .build();
    }

    @Override
    public CommentResponse getCommentsByIssueKeyAndCommentId(String issueKey, String commentId) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        helperMethod.requireNonBlank(commentId, "commentId is required");

        String url = commentByIdUrl(credentials.getBaseUrl(), issueKey, commentId);
        return jiraClientConfiguration.get(url, CommentResponse.class,
                credentials.getUsername(), credentials.getToken());
    }

    @Override
    public String findIssueKeyByCommentId(String jiraCommentId) {
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");
        CommentResponse commentResponse = getCommentById(jiraCommentId);
        if (commentResponse == null) {
            throw new ResourceNotFoundException("Jira comment not found or inaccessible for id: " + jiraCommentId);
        }
        return helperMethod.tryExtractIssueKeyFromSelf(commentResponse.getSelf());
    }

    /* ====================== Sync Jira → Local ====================== */

    @Override
    @Transactional
    public CommentResponse syncCommentByCommentId(String jiraCommentId) {
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");

        CommentResponse jiraCommentResponse = getCommentById(jiraCommentId);
        if (jiraCommentResponse == null) {
            throw new ResourceNotFoundException("Jira comment not found or inaccessible for id: " + jiraCommentId);
        }

        String issueKey = helperMethod.tryExtractIssueKeyFromSelf(jiraCommentResponse.getSelf());
        if (!helperMethod.hasText(issueKey)) {
            log.error("Could not extract issueKey from Jira comment self-URL: {}", jiraCommentResponse.getSelf());
            throw new IllegalStateException("Cannot synchronize comment; missing parent issue key.");
        }

        Issue managedIssue = ensureIssuePresentLocally(issueKey);
        saveCommentFromJira(managedIssue, jiraCommentResponse);
        return jiraCommentResponse;
    }

    @Override
    @Transactional
    public int synchronizeComments(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        log.info("Starting synchronization of all comments for issue key: {}", issueKey);

        Issue managedIssue = ensureIssuePresentLocally(issueKey);
        List<CommentResponse> jiraComments = getCommentsByIssueKey(issueKey);

        Set<String> jiraIds = jiraComments.stream()
                .map(CommentResponse::getId)
                .filter(helperMethod::hasText)
                .collect(Collectors.toSet());

        List<Comment> locals = commentRepository.findAllByIssue_Key(issueKey);

        List<Comment> toDelete = locals.stream()
                .filter(c -> helperMethod.hasText(c.getCommentId()) && !jiraIds.contains(c.getCommentId()))
                .toList();
        if (!toDelete.isEmpty()) { commentRepository.deleteAll(toDelete); }

        for (CommentResponse jiraComment : jiraComments) {
            saveCommentFromJira(managedIssue, jiraComment);
        }

        log.info("Finished synchronization of {} comments for issue key: {}", jiraComments.size(), issueKey);
        return jiraComments.size();
    }

    @Override
    @Transactional
    public CommentResponse synchronizeSingleCommentForIssue(Issue issue, CommentResponse comment) {
        if (comment == null) {
            throw new IllegalArgumentException("comment is required");
        }
        helperMethod.requireNonBlank(comment.getId(), "jiraCommentId is required");

        // Resolve (and load) the parent Issue that must already exist locally
        Issue managedIssue;
        if (issue != null) {
            String key = issue.getKey();
            helperMethod.requireNonBlank(key, "Provided Issue must have a non-blank key.");
            String fromSelf = helperMethod.tryExtractIssueKeyFromSelf(comment.getSelf());
            if (helperMethod.hasText(fromSelf) && !fromSelf.equals(key)) {
                log.warn("Comment {} self points to issue {}, but provided Issue key is {}.",
                        comment.getId(), fromSelf, key);
            }
            managedIssue = issueRepository.findByKey(key)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Issue not present locally: " + key + ". Sync the Issue first."));
        } else {
            // Derive key from the comment self URL
            String issueKey = helperMethod.tryExtractIssueKeyFromSelf(comment.getSelf());
            if (!helperMethod.hasText(issueKey)) {
                throw new IllegalStateException("Cannot synchronize comment; missing parent issue key.");
            }
            managedIssue = issueRepository.findByKey(issueKey)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Issue not present locally: " + issueKey + ". Sync the Issue first."));
        }

        // Upsert the comment under the managed Issue
        saveCommentFromJira(managedIssue, comment);
        return comment;
    }


    @Override
    @Transactional
    public int synchronizeCommentsByIssueKey(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        Issue managedIssue = ensureIssuePresentLocally(issueKey);
        List<CommentResponse> jiraComments = getCommentsByIssueKey(issueKey);

        Set<String> jiraIds = jiraComments.stream()
                .map(CommentResponse::getId)
                .filter(helperMethod::hasText)
                .collect(Collectors.toSet());

        List<Comment> locals = commentRepository.findAllByIssue_Key(managedIssue.getKey());

        List<Comment> toDelete = locals.stream()
                .filter(c -> helperMethod.hasText(c.getCommentId()) && !jiraIds.contains(c.getCommentId()))
                .toList();
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} local comments for issue {} that are missing in Jira.", toDelete.size(), issueKey);
            commentRepository.deleteAll(toDelete);
        }

        for (CommentResponse jiraComment : jiraComments) {
            saveCommentFromJira(managedIssue, jiraComment);
        }

        log.info("Finished synchronization of all comments for issue key: {}", issueKey);
        return jiraComments.size();
    }

    @Override
    @Transactional
    public CommentResponse synchronizeACommentForIssue(String jiraCommentId) {
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");

        CommentResponse jiraCommentResponse = getCommentById(jiraCommentId);
        if (jiraCommentResponse == null) {
            log.warn("Jira comment not found or inaccessible for id: {}", jiraCommentId);
            throw new ResourceNotFoundException("Jira comment not found or inaccessible for id: " + jiraCommentId);
        }

        String issueKey = helperMethod.tryExtractIssueKeyFromSelf(jiraCommentResponse.getSelf());
        if (!helperMethod.hasText(issueKey)) {
            throw new IllegalStateException("Cannot synchronize comment; missing parent issue key for Jira ID: " + jiraCommentId);
        }

        Issue managedIssue = ensureIssuePresentLocally(issueKey);
        saveCommentFromJira(managedIssue, jiraCommentResponse);
        return jiraCommentResponse;
    }

    @Override
    @Transactional
    public void synchronizeCommentsByIdsList(String issueKey, List<String> jiraCommentIds) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        if (jiraCommentIds == null || jiraCommentIds.isEmpty()) {
            log.info("Comment IDs list is null or empty for issue {}. Skipping synchronization.", issueKey);
            return;
        }

        Issue managedIssue = ensureIssuePresentLocally(issueKey);

        List<String> validIds = jiraCommentIds.stream()
                .filter(helperMethod::hasText)
                .distinct()
                .toList();

        if (validIds.isEmpty()) {
            log.info("After validation, no valid comment IDs remain for issue {}. Skipping synchronization.", issueKey);
            return;
        }

        JiraCommentIdsList payload = JiraCommentIdsList.builder().ids(validIds).build();
        log.info("Starting bulk synchronization of {} comments for issue {}.", validIds.size(), issueKey);

        CommentListResponse listResponse = getCommentByCommentIdsList(payload);
        List<CommentResponse> jiraComments = (listResponse != null && listResponse.getComments() != null)
                ? listResponse.getComments()
                : List.of();

        for (CommentResponse jiraComment : jiraComments) {
            saveCommentFromJira(managedIssue, jiraComment);
        }

        log.info("Successfully synchronized {} comments (out of {} requested) for issue {}.",
                jiraComments.size(), validIds.size(), issueKey);

    }

    @Override
    public void saveCommentFromJiraByIssueKey(String issueKey, CommentResponse jiraComment) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        if (jiraComment == null || !helperMethod.hasText(jiraComment.getId())) return;

        Issue managedIssue = ensureIssuePresentLocally(issueKey);
        saveCommentFromJira(managedIssue, jiraComment);
    }

    /* ====================== Local persistence helpers ====================== */

    @Override
    public void saveCommentFromJira(Issue issue, CommentResponse jiraComment) {
        if (issue == null || jiraComment == null || !helperMethod.hasText(jiraComment.getId())) return;

        Optional<Comment> localOpt = commentRepository.findByCommentId(jiraComment.getId());

        Comment local = localOpt.orElseGet(() -> commentMapper.mapToCommentEntity(jiraComment));
        if (localOpt.isPresent()) {
            commentMapper.updateCommentEntity(local, jiraComment);
        }

        // Ensure managed Issue is attached (nullable=false FK)
        local.setIssue(issue);
        local.setSynchronizationStatus(SynchronizationStatus.SYNCED);
        local.setPushStatus(PushStatus.PUSHED);

        commentRepository.save(local);
    }

    /* ====================== Ensure Issue exists (managed) ====================== */
    /**
     * Ensure the Issue already exists locally and is managed in the current transaction.
     * We intentionally DO NOT call any IssueService here (to avoid circular deps).
     * If the Issue is missing, we throw; callers should sync Issues beforehand.
     */
    @Transactional
    public Issue ensureIssuePresentLocally(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        return issueRepository.findByKey(issueKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issue not present locally: " + issueKey + ". Sync the Issue first."));
    }
}
