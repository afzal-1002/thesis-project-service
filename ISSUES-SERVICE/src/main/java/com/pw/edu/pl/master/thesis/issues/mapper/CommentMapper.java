package com.pw.edu.pl.master.thesis.issues.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.edu.pl.master.thesis.issues.dto.comment.CreateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Visibility;
import com.pw.edu.pl.master.thesis.issues.enums.PushStatus;
import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.model.comment.Comment;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor @Slf4j
public class CommentMapper {

    private final ObjectMapper objectMapper;
    private final HelperMethod helper;

    /* ──────────────────────────────────────────────────────────
     * Entity -> DTO
     * ────────────────────────────────────────────────────────── */
    public CommentResponse toCommentResponse(Comment entity) {
        if (entity == null) return null;

        // Use the unified parse method
        Body body = parseADF(entity.getBody());
        if (body == null) {
            body = createMinimalBody(entity.getBody());
        }

        return CommentResponse.builder()
                .id(safe(entity.getCommentId()))
                .self(safe(entity.getSelf()))
                .body(body)
                .visibility(entity.getVisibility())
                .jsdPublic(entity.getJsdPublic())
                .created(entity.getCreatedAt())
                .updated(entity.getUpdatedAt())
                .author(minimalUser(entity.getAuthorId()))
                .updateAuthor(minimalUser(entity.getUpdatedByUserId()))
                .build();
    }

    // Alias for the old mapToCommentResponse
    public CommentResponse mapToCommentResponse(Comment entity) {
        return toCommentResponse(entity);
    }

    /* ──────────────────────────────────────────────────────────
     * DTO -> Entity (preferred) — requires Issue (not null)
     * ────────────────────────────────────────────────────────── */
    public Comment mapToCommentEntity(CommentResponse dto, Issue issue) {
        if (dto == null) return null;
        if (issue == null) throw new IllegalArgumentException("Issue must be provided (Comment.issue is nullable=false).");

        Comment.CommentBuilder b = Comment.builder();

        b.commentId(safe(dto.getId()));
        b.self(safe(dto.getSelf()));
        b.synchronizationStatus(SynchronizationStatus.SYNCED);
        b.pushStatus(PushStatus.PUSHED); // Jira entity is considered pushed

        // relation
        b.issue(issue);

        // authors
        if (dto.getAuthor() != null) {
            b.authorId(safe(dto.getAuthor().getAccountId()));
        }
        if (dto.getUpdateAuthor() != null) {
            b.updatedByUserId(safe(dto.getUpdateAuthor().getAccountId()));
        }

        // body (ADF JSON)
        b.body(serializeADF(dto.getBody()));

        // visibility + flags
        Visibility vis = dto.getVisibility();
        if (vis != null) b.visibility(vis);
        b.jsdPublic(dto.getJsdPublic());

        // audit
        OffsetDateTime created = dto.getCreated();
        OffsetDateTime updated = dto.getUpdated();
        b.createdAt(created);
        b.updatedAt(updated != null ? updated : created);

        return b.build();
    }

    /* ──────────────────────────────────────────────────────────
     * DTO -> Entity (fallback) — WITHOUT Issue (attach later)
     * ────────────────────────────────────────────────────────── */
    public Comment mapToCommentEntity(CommentResponse dto) {
        if (dto == null) return null;

        Comment.CommentBuilder b = Comment.builder()
                .commentId(safe(dto.getId()))
                .self(safe(dto.getSelf()))
                .synchronizationStatus(SynchronizationStatus.SYNCED)
                .pushStatus(PushStatus.PUSHED)
                .body(serializeADF(dto.getBody()))
                .visibility(dto.getVisibility())
                .jsdPublic(dto.getJsdPublic())
                .createdAt(dto.getCreated())
                .updatedAt(dto.getUpdated() != null ? dto.getUpdated() : dto.getCreated());

        if (dto.getAuthor() != null) {
            b.authorId(safe(dto.getAuthor().getAccountId()));
        }
        if (dto.getUpdateAuthor() != null) {
            b.updatedByUserId(safe(dto.getUpdateAuthor().getAccountId()));
        }

        return b.build();
    }

    /* ──────────────────────────────────────────────────────────
     * Entity Update Logic (Upsert from Jira)
     * ────────────────────────────────────────────────────────── */
    public void updateCommentEntity(Comment entity, CommentResponse dto) {
        if (entity == null || dto == null) { return; }

        // --- Core Data Fields ---
        entity.setCommentId(dto.getId());
        entity.setSelf(safe(dto.getSelf())); // Added self-URL for completeness
        entity.setVisibility(dto.getVisibility()); // Added visibility
        entity.setJsdPublic(dto.getJsdPublic()); // Added jsdPublic

        // Body: Store the structured ADF JSON in the local entity
        entity.setBody(serializeADF(dto.getBody()));

        // --- Timestamps & Authors ---
        if (dto.getUpdated() != null) { entity.setUpdatedAt(dto.getUpdated()); }
        if (dto.getCreated() != null) { entity.setCreatedAt(dto.getCreated()); } // Should not change, but safe to update

        // Assuming your Comment entity stores the author's account ID
        if (dto.getAuthor() != null) { entity.setAuthorId(safe(dto.getAuthor().getAccountId())); }

        // --- Synchronization Status ---
        entity.setSynchronizationStatus(SynchronizationStatus.SYNCED);
        entity.setPushStatus(PushStatus.PUSHED);
    }

    /* ──────────────────────────────────────────────────────────
     * Local -> Request DTO (for pushing to Jira)
     * ────────────────────────────────────────────────────────── */
    public CreateCommentRequest buildCreateRequestFromLocal(Comment comment) {
        Body adf = parseADF(comment.getBody());
        if (adf == null) { adf = createMinimalBody(comment.getBody()); }

        return CreateCommentRequest.builder()
                .body(adf)
                .build();
    }


    /* ──────────────────────────────────────────────────────────
     * Internal JSON/ADF Helpers
     * ────────────────────────────────────────────────────────── */

    // Merged parseBody and parseADF
    private Body parseADF(String json) {
        if (!helper.hasText(json)) return null;
        try {
            return objectMapper.readValue(json, Body.class);
        } catch (Exception e) {
            log.warn("Failed to parse ADF json for local comment: {}", e.getMessage());
            return null;
        }
    }

    // Helper to get ADF from entity.body if entity.body is structured JSON
    // Renamed from toJson to be more specific
    private String serializeADF(Body doc) {
        try {
            if (doc == null) doc = createMinimalBody(null);
            return objectMapper.writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize ADF body: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createMinimalBody(null));
            } catch (JsonProcessingException ignored) {
                return "{\"type\":\"doc\",\"version\":1}";
            }
        }
    }

    // Unified minimal body creation
    private Body createMinimalBody(String str) {
        if (str == null) str = "";

        Body.Content textNode = Body.Content.builder()
                .type("text")
                .text(str)
                .build();

        Body.Content paragraph = Body.Content.builder()
                .type("paragraph")
                .content(List.of(textNode))
                .build();

        return Body.builder()
                .type("doc")
                .version(1)
                .content(List.of(paragraph))
                .build();
    }

    // Alias for the zero-arg version (used in older code)
    private Body minimalBody() {
        return createMinimalBody(null);
    }

    private String extractPlainTextFromBody(Body body) {
        if (body == null || body.getContent() == null) return null;
        return body.getContent().stream()
                .flatMap(content -> content.getContent() != null ? content.getContent().stream() : null)
                .filter(nested -> nested != null && "text".equals(nested.getType()))
                .map(Body.Content::getText)
                .collect(Collectors.joining("\n"));
    }

    private String toJson(Object obj) {
        return serializeADF((Body) obj);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private UserSummary minimalUser(String accountId) {
        if (accountId == null || accountId.isBlank()) return null;
        UserSummary u = new UserSummary();
        u.setAccountId(accountId);
        return u;
    }
}