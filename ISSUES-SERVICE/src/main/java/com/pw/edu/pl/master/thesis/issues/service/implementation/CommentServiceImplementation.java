package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.edu.pl.master.thesis.issues.dto.comment.CreateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.comment.UpdateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.enums.PushStatus;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.mapper.CommentMapper;
import com.pw.edu.pl.master.thesis.issues.model.comment.Comment;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import com.pw.edu.pl.master.thesis.issues.repository.CommentRepository;
import com.pw.edu.pl.master.thesis.issues.repository.IssueRepository;
import com.pw.edu.pl.master.thesis.issues.service.CommentService;
import com.pw.edu.pl.master.thesis.issues.service.JiraCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImplementation implements CommentService {

    private final JiraCommentService jiraCommentService;
    private final CommentRepository  commentRepository;
    private final HelperMethod       helperMethod;
    private final ObjectMapper       objectMapper;   // kept if needed elsewhere
    private final CommentMapper      commentMapper;
    private final IssueRepository    issueRepository;

    /* -------------------------------------------------------------
     * Jira-first: create in Jira -> upsert locally
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public CommentResponse createComment(String issueKey, CreateCommentRequest request) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        helperMethod.requireNonNull(request, "CreateCommentRequest is required");
        helperMethod.requireNonNull(request.getBody(), "CreateCommentRequest.body is required");

        CommentResponse jiraComment = jiraCommentService.addFullComment(issueKey, request);
        // Persist under the (already existing) local Issue.
        jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, jiraComment);
        return jiraComment;
    }

    /* -------------------------------------------------------------
     * Jira-first: update in Jira -> upsert locally
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public CommentResponse updateComment(String issueKey, String jiraCommentId, UpdateCommentRequest request) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");
        helperMethod.requireNonNull(request, "UpdateCommentRequest is required");
        helperMethod.requireNonNull(request.getBody(), "UpdateCommentRequest.body is required");

        // Ensure comment exists in Jira (404 → throws)
        jiraCommentService.getCommentsByIssueKeyAndCommentId(issueKey, jiraCommentId);

        CommentResponse updated = jiraCommentService.updateCommentByIssueKeyCommentId(issueKey, jiraCommentId, request);
        jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, updated);
        return updated;
    }

    /* -------------------------------------------------------------
     * Jira-first: delete
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public void deleteComment(String issueKey, String jiraCommentId) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");

        jiraCommentService.deleteComment(issueKey, jiraCommentId);
        commentRepository.findByCommentId(jiraCommentId).ifPresent(commentRepository::delete);
    }

    /* -------------------------------------------------------------
     * Sync (Jira fresh → local)
     * ----------------------------------------------------------- */


    @Override
    @Transactional
    public String syncCommentsFromJira(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        // 1) Fetch from Jira (null-safe)
        List<CommentResponse> jiraComments = Optional
                .ofNullable(jiraCommentService.getCommentsByIssueKey(issueKey))
                .orElse(List.of());

        // 2) Load locals BEFORE delete to compute created/updated later
        List<Comment> locals = commentRepository.findAllByIssue_Key(issueKey);

        // Build sets for quick lookup
        Set<String> jiraIds = jiraComments.stream()
                .map(CommentResponse::getId)
                .filter(helperMethod::hasText)
                .collect(Collectors.toSet());

        Set<String> localIds = locals.stream()
                .map(Comment::getCommentId)
                .filter(helperMethod::hasText)
                .collect(Collectors.toSet());

        // 3) Delete locals that disappeared in Jira
        List<Comment> toDelete = locals.stream()
                .filter(c -> helperMethod.hasText(c.getCommentId()) && !jiraIds.contains(c.getCommentId()))
                .toList();
        int deleted = 0;
        if (!toDelete.isEmpty()) {
            deleted = toDelete.size();
            commentRepository.deleteAll(toDelete);
        }

        // 4) Upsert each Jira comment under existing Issue (count created vs updated)
        int created = 0;
        int updated = 0;
        for (CommentResponse dto : jiraComments) {
            if (!helperMethod.hasText(dto.getId())) {
                continue; // skip invalid Jira payloads defensively
            }
            boolean exists = localIds.contains(dto.getId());
            jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, dto);
            if (exists) updated++; else created++;
        }

        // 5) Return summary
        int fetched = jiraComments.size();
        return "Comments synchronized for issue " + issueKey +
                " (fetched=" + fetched +
                ", created=" + created +
                ", updated=" + updated +
                ", deleted=" + deleted + ").";
    }


    /* -------------------------------------------------------------
     * Read (Jira fresh → upsert local → return Jira DTOs)
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public List<CommentResponse> findAllByIssueKey(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        List<CommentResponse> jiraList = jiraCommentService.getCommentsByIssueKey(issueKey);
        for (CommentResponse c : jiraList) {
            jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, c);
        }
        return jiraList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getAllLocalByIssueKey(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        List<Comment> locals = Optional
                .ofNullable(commentRepository.findAllByIssue_Key(issueKey))
                .orElse(List.of());

        if (locals.isEmpty()) return List.of();

        // Sort locally by DB primary key (stable); change to getCreatedAt() if you prefer chronological order
        return locals.stream()
                .sorted(Comparator.comparing(Comment::getId))
                .map(commentMapper::toCommentResponse)
                .toList();
    }


    /* -------------------------------------------------------------
     * Reads: Local only
     * ----------------------------------------------------------- */
    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(String id) {
        Comment entity = commentRepository.findByCommentId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: id=" + id));
        return commentMapper.toCommentResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentByJiraId(String jiraCommentId) {
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");
        Comment entity = commentRepository.findByCommentId(jiraCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found for Jira id: " + jiraCommentId));
        return commentMapper.toCommentResponse(entity);
    }

    /* -------------------------------------------------------------
     * Local update/delete by local PK (proxy to Jira if possible)
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request) {
        Comment local = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: id=" + id));
        String issueKey      = (local.getIssue() != null) ? local.getIssue().getKey() : null;
        String jiraCommentId = local.getCommentId();
        if (!helperMethod.hasText(issueKey) || !helperMethod.hasText(jiraCommentId)) {
            throw new IllegalStateException("Local comment missing issueKey or jiraCommentId.");
        }

        CommentResponse updated = jiraCommentService.updateCommentByIssueKeyCommentId(issueKey, jiraCommentId, request);
        jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, updated);
        return updated;
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment local = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: id=" + id));

        String issueKey      = (local.getIssue() != null) ? local.getIssue().getKey() : null;
        String jiraCommentId = local.getCommentId();

        if (helperMethod.hasText(issueKey) && helperMethod.hasText(jiraCommentId)) {
            try {
                jiraCommentService.deleteComment(issueKey, jiraCommentId);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                    log.warn(e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Failed deleting in Jira (continuing local delete) id={} jiraId={} cause={}",
                        id, jiraCommentId, e.getMessage());
            }
        }
        commentRepository.delete(local);
    }

    /* -------------------------------------------------------------
     * Batch local read by Jira ids (Jira fresh, upsert local)
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public List<CommentResponse> getCommentsByJiraIds(List<String> jiraCommentIds) {
        if (jiraCommentIds == null || jiraCommentIds.isEmpty()) return List.of();

        List<String> ids = jiraCommentIds.stream()
                .filter(helperMethod::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        List<CommentResponse> results = new ArrayList<>(ids.size());

        for (String jiraId : ids) {
            String issueKey = commentRepository.findByCommentId(jiraId)
                    .map(c -> c.getIssue() != null ? c.getIssue().getKey() : null)
                    .orElse(null);

            if (!helperMethod.hasText(issueKey)) {
                log.warn("Skipping Jira comment id {} — missing local mapping to an issueKey.", jiraId);
                continue;
            }

            try {
                CommentResponse resp = jiraCommentService.getCommentsByIssueKeyAndCommentId(issueKey, jiraId);
                if (resp != null) {
                    jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, resp);
                    results.add(resp);
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.warn("Jira returned 404 for issueKey={}, commentId={}. Skipping.", issueKey, jiraId);
                    continue;
                }
                throw e;
            }
        }
        return results;
    }

    /* -------------------------------------------------------------
     * Extra util: fetch a Jira comment by id, upsert locally
     * ----------------------------------------------------------- */
    @Override
    @Transactional
    public CommentResponse findAllById(String jiraCommentId) {
        helperMethod.requireNonBlank(jiraCommentId, "jiraCommentId is required");

        CommentResponse jiraCommentResponse = jiraCommentService.getCommentById(jiraCommentId);
        if (jiraCommentResponse == null) {
            throw new ResourceNotFoundException("Jira comment not found for id: " + jiraCommentId);
        }

        String issueKey = commentRepository.findByCommentId(jiraCommentId)
                .map(c -> c.getIssue() != null ? c.getIssue().getKey() : null)
                .orElseGet(() -> helperMethod.tryExtractIssueKeyFromSelf(jiraCommentResponse.getSelf()));

        if (helperMethod.hasText(issueKey)) {
            jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, jiraCommentResponse);
        } else {
            log.warn("Could not resolve issueKey for Jira comment id={}. Skipping upsert.", jiraCommentId);
        }

        return jiraCommentResponse;
    }

    /* =========================
     *  Sync by local Issue PK
     * ========================= */
    @Override
    @Transactional
    public String syncFromJiraByIssueId(String issueId) {
        helperMethod.requireNonBlank(issueId, "issueId is required");

        Issue issue = issueRepository.findByJiraId(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Local issue not found for jiraId=" + issueId));

        String issueKey = issue.getKey();
        if (!helperMethod.hasText(issueKey)) {
            throw new IllegalStateException("Issue has no key for jiraId=" + issueId);
        }

        // Reuse the detailed message from comment sync
        String commentsMsg = syncCommentsFromJira(issueKey);
        return "Issue with key " + issueKey + " synchronized by jiraId " + issueId + ". " + commentsMsg;
    }


    /* ======================================================
     *  Local → Jira (push a single local comment)
     * ====================================================== */
    @Override
    @Transactional
    public CommentResponse pushLocalCommentToJira(String issueKey, String localCommentId) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        if (localCommentId == null) throw new IllegalArgumentException("localCommentId is required");

        Comment local = commentRepository.findByCommentId(localCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Local comment not found: id=" + localCommentId));

        // Already in Jira?
        if (helperMethod.hasText(local.getCommentId())) {
            CommentResponse resp = jiraCommentService.getCommentsByIssueKeyAndCommentId(issueKey, local.getCommentId());
            if (resp != null) {
                jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, resp);
                return resp;
            }
        }

        // Build request from local entity and push
        CreateCommentRequest payload = commentMapper.buildCreateRequestFromLocal(local);
        CommentResponse created = jiraCommentService.addFullComment(issueKey, payload);
        jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, created);
        return created;
    }

    /* ======================================================
     *  Local → Jira (bulk push by IDs)
     * ====================================================== */
    @Override
    @Transactional
    public List<CommentResponse> pushLocalCommentsToJira(String issueKey, List<String> localCommentIds) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");
        if (localCommentIds == null || localCommentIds.isEmpty()) return List.of();

        List<CommentResponse> out = new ArrayList<>();
        for (String id : localCommentIds) {
            try {
                CommentResponse r = pushLocalCommentToJira(issueKey, id);
                if (r != null) out.add(r);
            } catch (Exception e) {
                log.warn("Failed pushing local comment id={} to Jira issue {}: {}", id, issueKey, e.getMessage());
            }
        }
        return out;
    }

    /* =============================================================================
     *  Local → Jira (push ALL pending for an issue)
     * ============================================================================= */
    @Override
    @Transactional
    public List<CommentResponse> pushAllPendingCommentsToJira(String issueKey) {
        helperMethod.requireNonBlank(issueKey, "issueKey is required");

        List<Comment> pending = commentRepository.findAllByIssue_Key(issueKey).stream()
                .filter(c -> !helperMethod.hasText(c.getCommentId())
                        || c.getPushStatus() == PushStatus.DRAFT
                        || c.getPushStatus() == PushStatus.PENDING
                        || c.getPushStatus() == PushStatus.FAILED)
                .toList();

        List<CommentResponse> results = new ArrayList<>(pending.size());
        for (Comment local : pending) {
            try {
                CommentResponse created = jiraCommentService.addFullComment(issueKey, commentMapper.buildCreateRequestFromLocal(local));
                jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, created);
                results.add(created);
            } catch (HttpClientErrorException e) {
                log.warn("Jira error pushing local comment id={} to {}: status={} body={}",
                        local.getId(), issueKey, e.getStatusCode(), e.getResponseBodyAsString());
                local.setPushStatus(PushStatus.FAILED);
                commentRepository.save(local);
            } catch (Exception e) {
                log.warn("Error pushing local comment id={} to {}: {}", local.getId(), issueKey, e.getMessage());
                local.setPushStatus(PushStatus.FAILED);
                commentRepository.save(local);
            }
        }
        return results;
    }

    /* =========================================================================
     *  Local → Jira (push ALL pending across ALL issues)
     * ========================================================================= */
    @Override
    @Transactional
    public List<CommentResponse> pushAllPendingCommentsEverywhere() {
        List<Comment> pending = commentRepository.findAllPendingForPush();
        List<CommentResponse> out = new ArrayList<>(pending.size());

        for (Comment local : pending) {
            String issueKey = (local.getIssue() != null) ? local.getIssue().getKey() : null;
            if (!helperMethod.hasText(issueKey)) {
                log.warn("Skipping local comment id={} — missing issue key.", local.getId());
                continue;
            }
            try {
                CommentResponse created = jiraCommentService.addFullComment(issueKey, commentMapper.buildCreateRequestFromLocal(local));
                jiraCommentService.saveCommentFromJiraByIssueKey(issueKey, created);
                out.add(created);
            } catch (Exception e) {
                log.warn("Failed to push local comment id={} to {}: {}", local.getId(), issueKey, e.getMessage());
                local.setPushStatus(PushStatus.FAILED);
                commentRepository.save(local);
            }
        }
        return out;
    }

    /* =========================
     *  Get all AI comments
     * ========================= */
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getAllAiComments() {
        List<Comment> ai = commentRepository.findAllByAiTrue();
        return ai.stream().map(commentMapper::toCommentResponse).toList();
    }
}
