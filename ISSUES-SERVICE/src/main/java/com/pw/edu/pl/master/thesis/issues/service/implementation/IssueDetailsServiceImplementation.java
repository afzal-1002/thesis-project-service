package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.edu.pl.master.thesis.issues.dto.issue.issuedetails.CommentDetail;
import com.pw.edu.pl.master.thesis.issues.dto.issue.issuedetails.CommentMediaRef;
import com.pw.edu.pl.master.thesis.issues.dto.issue.issuedetails.IssueDetails;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.*;
import com.pw.edu.pl.master.thesis.issues.service.IssueDetailsService;
import com.pw.edu.pl.master.thesis.issues.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueDetailsServiceImplementation implements IssueDetailsService {

    private final JiraIssueService jiraIssueService;
    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public IssueDetails getIssueDetails(String issueKey) {
        // 1) Fetch full issue
        IssueResponse issue = jiraIssueService.getIssueByIdOrKey(issueKey);

        // 2) Title (summary)
        String title = safeSummary(issue);

        // 3) Description → plain text; detect inline media
        Body adfDescription = safeDescriptionAdf(issue);
        String descriptionPlain = adfToPlainText(OM, adfDescription);
        boolean descriptionHasMedia = adfHasMedia(OM, adfDescription);

        // 4) Issue has attachments (array) or inline ADF media
        boolean hasIssueAttachments = hasIssueAttachments(issue) || descriptionHasMedia;

        // 5) Comments → title + hasAttachment (via ADF media in body) + mediaRefs list
        List<CommentDetail> commentDetails = new ArrayList<>();
        List<CommentResponse> comments = safeCommentList(issue);

        if (comments != null) {
            for (CommentResponse c : comments) {
                if (c == null) continue;

                Body cAdf = c.getBody();
                String cPlain = adfToPlainText(OM, cAdf);
                String cTitle = firstLineTitle(cPlain);

                List<CommentMediaRef> mediaRefs = extractMediaRefs(OM, cAdf);
                boolean cHasAttachment = !mediaRefs.isEmpty();

                commentDetails.add(
                        CommentDetail.builder()
                                .title(cTitle)
                                .hasAttachment(cHasAttachment)
                                .mediaRefs(mediaRefs)
                                .build()
                );
            }
        }

        log.info("Issue '{}' → title='{}', descHasMedia={}, comments={}",
                issueKey, title, descriptionHasMedia, commentDetails.size());

        return IssueDetails.builder()
                .title(title)
                .description(descriptionPlain)
                .hasAttachment(hasIssueAttachments)
                .comments(commentDetails)
                .build();
    }

    /* ===================== null-safe getters ===================== */

    private static String safeSummary(IssueResponse issue) {
        try { return Objects.toString(issue.getFields().getSummary(), ""); }
        catch (Exception e) { return ""; }
    }

    private static Body safeDescriptionAdf(IssueResponse issue) {
        try { return issue.getFields().getDescription(); }
        catch (Exception e) { return null; }
    }

    private static boolean hasIssueAttachments(IssueResponse issue) {
        try {
            List<Attachment> atts = issue.getFields().getAttachment(); // mapped from "attachment"
            return atts != null && !atts.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private static List<CommentResponse> safeCommentList(IssueResponse issue) {
        try {
            CommentWrapper cw = issue.getFields().getComment();
            return (cw != null && cw.getComments() != null) ? cw.getComments() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /* ===================== ADF helpers ===================== */

    private static String adfToPlainText(ObjectMapper om, Object adfDocOrBody) {
        if (adfDocOrBody == null) return "";
        JsonNode root = om.valueToTree(adfDocOrBody);
        if (root == null) return "";

        StringBuilder sb = new StringBuilder();
        traverseText(root, sb);
        return sb.toString().trim();
    }

    private static boolean adfHasMedia(ObjectMapper om, Object adfDocOrBody) {
        return !extractMediaRefs(om, adfDocOrBody).isEmpty();
    }

    /** Collect media refs (media/mediaSingle/mediaGroup) from an ADF node tree. */
    private static List<CommentMediaRef> extractMediaRefs(ObjectMapper om, Object adfDocOrBody) {
        if (adfDocOrBody == null) return Collections.emptyList();
        JsonNode root = om.valueToTree(adfDocOrBody);
        if (root == null) return Collections.emptyList();

        List<CommentMediaRef> out = new ArrayList<>();
        ArrayDeque<JsonNode> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            JsonNode n = stack.pop();

            if (n.isObject()) {
                JsonNode type = n.get("type");
                if (type != null && type.isTextual()) {
                    String t = type.asText();
                    if ("media".equals(t)) {
                        // Expected shape:
                        // { "type":"media", "attrs":{"type":"file","id":"...","collection":"..."} }
                        JsonNode attrs = n.get("attrs");
                        if (attrs != null && attrs.isObject()) {
                            String id = textOrNull(attrs.get("id"));
                            String mType = textOrNull(attrs.get("type"));
                            String collection = textOrNull(attrs.get("collection"));
                            if (id != null || mType != null || collection != null) {
                                out.add(CommentMediaRef.builder()
                                        .id(id)
                                        .type(mType)
                                        .collection(collection)
                                        .build());
                            }
                        }
                    }
                }
                n.fields().forEachRemaining(e -> stack.push(e.getValue()));
            } else if (n.isArray()) {
                n.forEach(stack::push);
            }
        }
        return out;
    }

    private static String textOrNull(JsonNode node) {
        return (node != null && node.isTextual()) ? node.asText() : null;
    }

    private static void traverseText(JsonNode node, StringBuilder sb) {
        if (node == null) return;
        if (node.isObject()) {
            JsonNode type = node.get("type");
            if (type != null && type.isTextual()) {
                String t = type.asText();
                if ("text".equals(t)) {
                    JsonNode text = node.get("text");
                    if (text != null && text.isTextual()) sb.append(text.asText());
                } else if ("hardBreak".equals(t)) {
                    sb.append('\n');
                }
            }
            node.fields().forEachRemaining(e -> traverseText(e.getValue(), sb));
            if (type != null && "paragraph".equals(type.asText())) sb.append('\n');
        } else if (node.isArray()) {
            node.forEach(child -> traverseText(child, sb));
        }
    }

    private static String firstLineTitle(String plainText) {
        if (plainText == null) return "";
        for (String ln : plainText.split("\\R")) {
            String t = ln.trim();
            if (!t.isEmpty()) return (t.length() <= 80) ? t : t.substring(0, 80) + "…";
        }
        return "";
    }
}