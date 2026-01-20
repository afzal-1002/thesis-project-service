package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.pw.edu.pl.master.thesis.issues.client.ProjectClient;
import com.pw.edu.pl.master.thesis.issues.dto.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.request.JqlSearchRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.IssueResponseSummary;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueFields;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.ProjectSummary;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeReference;
import com.pw.edu.pl.master.thesis.issues.dto.project.ProjectReference;
import com.pw.edu.pl.master.thesis.issues.dto.project.ProjectResponse;
import com.pw.edu.pl.master.thesis.issues.dto.project.SyncProjectRequest;
import com.pw.edu.pl.master.thesis.issues.dto.user.UserReference;
import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.exception.ProjectNotFoundException;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.mapper.IssueMapper;
import com.pw.edu.pl.master.thesis.issues.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueFields;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import com.pw.edu.pl.master.thesis.issues.model.issue.IssueTimeTracking;
import com.pw.edu.pl.master.thesis.issues.model.issuetype.IssueType;
import com.pw.edu.pl.master.thesis.issues.repository.IssueRepository;
import com.pw.edu.pl.master.thesis.issues.service.*;
import com.pw.edu.pl.master.thesis.issues.service.IssueService;
import com.pw.edu.pl.master.thesis.issues.service.IssueTypeService;
import com.pw.edu.pl.master.thesis.issues.service.JiraCommentService;
import com.pw.edu.pl.master.thesis.issues.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service @Slf4j
@RequiredArgsConstructor
public class IssueServiceImplementation implements IssueService {

    private final JiraIssueService jiraIssueService;
    private final IssueTypeService issueTypeService;
    private final ProjectClient projectClient;
    private final HelperMethod helperMethod;
    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final @Lazy JiraCommentService jiraCommentService;

    @Override
    @Transactional
    public IssueResponse createIssue(CreateIssueRequest request) {
        // 1) Validate

        helperMethod.requireNonNull(request, "request is null");

        final CreateIssueFields fields = request.getFields();
        final ProjectReference projRef = Objects.requireNonNull(fields.getProject(),  "Project reference is required");
        final IssueTypeReference typeRef = Objects.requireNonNull(fields.getIssuetype(),"Issue type reference is required");

        // 2) Normalize project key for Jira create
        ProjectResponse project = resolveProjectByKeyOrThrow(projRef.getKey());
        fields.getProject().setKey(project.getKey());

        // 3) Resolve local IssueType (prefer Jira id, fallback to name) — do NOT overwrite outbound Jira id
        Long jiraIssueTypeId = typeRef.getId(); // should be Jira's issuetype id
        String jiraIssueTypeName = typeRef.getName();

        IssueType issueTypeEntity = issueTypeService.findOrCreateBIdOrName(
                (jiraIssueTypeId == null ? null : String.valueOf(jiraIssueTypeId)),
                jiraIssueTypeName
        );
        if (issueTypeEntity == null) {
            throw new IllegalStateException("Unable to resolve or create local IssueType (jiraId="
                    + jiraIssueTypeId + ", name=" + jiraIssueTypeName + ")");
        }

//        IssueTypeSummary jiraIssue = jiraIssueService.

        // 4) Create in Jira
        IssueResponseSummary createdSummary = jiraIssueService.createIssue(request);

        // 5) Fetch full Jira issue
        IssueResponse jiraIssue = jiraIssueService.getIssueByKeyJira(createdSummary.getKey());
        if (jiraIssue == null || jiraIssue.getFields() == null) {
            throw new IllegalStateException("Created in Jira but could not fetch full issue by key: " + createdSummary.getKey());
        }

        // 6) Map to local entity
        Issue entity = issueMapper.toIssueEntity(jiraIssue);

        // Ensure required FKs/fields
        entity.setIssueType(issueTypeEntity); // fills issue_type_id (NOT NULL)

        // 6a) Wire back-references for children that own FKs
        IssueTimeTracking tt = entity.getTimeTracking();
        if (tt != null) {
            boolean hasData =
                    (tt.getTimeSpent() != null)
                            || (tt.getOriginalEstimate() != null && !tt.getOriginalEstimate().isBlank())
                            || (tt.getRemainingEstimate() != null && !tt.getRemainingEstimate().isBlank())
                            || (tt.getOriginalEstimateSeconds() != null)
                            || (tt.getRemainingEstimateSeconds() != null)
                            || (tt.getAiEstimateMinutes() != null);

            if (hasData) {
                tt.setIssue(entity);              // make sure FK issue_id is set
            } else {
                entity.setTimeTracking(null);     // drop empty child to avoid null-FK insert
            }
        }


        // 6b) Initialize timestamps if your entity expects them non-null
        if (entity.getCreatedAt() == null) entity.setCreatedAt(java.time.LocalDateTime.now());
        if (entity.getUpdatedAt() == null) entity.setUpdatedAt(java.time.LocalDateTime.now());

        // 7) Persist locally
        issueRepository.save(entity);

        // 8) Return Jira representation
        return jiraIssue;
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueByKey(String issueKey) {
        if (issueKey == null || issueKey.isBlank()) {
            throw new IllegalArgumentException("issueKey required");
        }

        IssueResponse jiraIssue = jiraIssueService.getIssueByKeyJira(issueKey);

        String projKey = Optional.ofNullable(jiraIssue.getFields())
                .map(IssueFields::getProject)
                .map(ProjectSummary::getKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project key missing in Jira response"));

        resolveProjectByKeyOrThrow(projKey);

        Issue entity = issueMapper.toIssueEntity(jiraIssue);
        issueRepository.save(entity);

        return jiraIssue;
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("issueId must be greater than 0");
        }

        IssueResponse jiraIssue = jiraIssueService.getIssueById(id);

        String projKey = Optional.ofNullable(jiraIssue.getFields())
                .map(IssueFields::getProject)
                .map(ProjectSummary::getKey)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Project key missing in Jira response for issueId: " + id
                ));

        resolveProjectByKeyOrThrow(projKey);

        Issue entity = issueMapper.toIssueEntity(jiraIssue);
        issueRepository.save(entity);

        return jiraIssue;
    }


    @Override
    @Transactional
    public List<IssueResponse> listIssuesForProjectKey(String projectKey) {
        if (projectKey == null || projectKey.isBlank()) {
            throw new IllegalArgumentException("projectKey required");
        }

        return synchronizeProjectIssues(projectKey);
    }

    @Override
    @Transactional
    public IssueResponse updateIssue(String issueKey, CreateIssueRequest request) {
        if (issueKey == null || issueKey.isBlank()) throw new IllegalArgumentException("issueKey required");

        IssueResponse updated = jiraIssueService.updateIssue(issueKey, request);
        issueRepository.save(issueMapper.toIssueEntity(updated));
        return updated;
    }

    @Override
    @Transactional
    public List<IssueResponse> synchronizeProjectIssues(String projectKey) {
        JqlSearchRequest request = jiraUrlBuilder.getJqlSearchRequest(projectKey);

        List<IssueResponse> issueResponses = jiraIssueService.getAllIssuesForProject(request);

        for (IssueResponse issueResponse : issueResponses) {
            CreateIssueRequest createIssueRequest = this.createIssueRequest(issueResponse.getKey());
            this.createIssue(createIssueRequest);
        }

        return issueResponses;
    }

    @Override
    @Transactional
    public List<IssueResponse> listIssueResponsesByProjectId(String projectKey) {
        return synchronizeProjectIssues(projectKey);
    }

    private ProjectResponse resolveProjectByKeyOrThrow(String projectKey) {
        SyncProjectRequest req = SyncProjectRequest.builder().projectKey(projectKey).build();
        ProjectResponse pr = projectClient.syncOne(req);
        if (pr == null || pr.getKey() == null) throw new ProjectNotFoundException("Project not found: " + projectKey);
        return pr;
    }

    private ProjectResponse resolveProjectByIdOrThrow(String projectKey) {
        ProjectResponse pr = projectClient.getProjectByKey(projectKey);
        if (pr == null || pr.getKey() == null) throw new ProjectNotFoundException("Project not found: id=" + projectKey);
        return pr;
    }

    @Override
    public CreateIssueRequest createIssueRequest(String issueKey) {
        IssueResponse src = jiraIssueService.getIssueByKeyJira(issueKey);
        IssueFields f = src.getFields();

        ProjectReference projectRef = ProjectReference.of(
                toLongOrNull(f.getProject().getId()),
                f.getProject().getKey()
        );

        // Use Jira’s issuetype id/name
        Long jiraIssueTypeId = toLongOrNull(f.getIssueTypeResponse().getId());
        IssueTypeReference issueTypeRef = IssueTypeReference.of(jiraIssueTypeId, f.getIssueTypeResponse().getName());

        UserReference assigneeRef = null;
        if (f.getAssignee() != null) {
            String accountId = (f.getAssignee().getAccountId() != null && !f.getAssignee().getAccountId().isBlank())
                    ? f.getAssignee().getAccountId()
                    : f.getAssignee().getId();
            assigneeRef = UserReference.of(accountId, f.getAssignee().getUsername());
        }

        CreateIssueFields fields = CreateIssueFields.builder()
                .project(projectRef)
                .issuetype(issueTypeRef)                // ← Jira id from source
                .summary(f.getSummary())
                .duedate(f.getDueDate() == null ? null : f.getDueDate().toString())
                .assignee(assigneeRef)
                .description(f.getDescription())
                .labels(f.getLabels())
                .build();

        return CreateIssueRequest.builder().fields(fields).build();
    }

    private static Long toLongOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.valueOf(s); } catch (NumberFormatException e) { return null; }
    }


    @Override
    @Transactional
    public IssueResponse synchronizeIssueByKey(String issueKey) {
        helperMethod.hasText(issueKey);

        // 1) Pull latest from Jira
        IssueResponse jiraIssue = jiraIssueService.getIssueByIdOrKey(issueKey);
        if (jiraIssue == null) {
            throw new ResourceNotFoundException("Jira issue not found for key=" + issueKey);
        }
        upsertFromJira(jiraIssue);

        return jiraIssue;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncIssueByIssueKey(String issueKey) {
        // 1) Pull issue from Jira
        IssueResponse jr = jiraIssueService.getIssueByIdOrKey(issueKey);

        // 2) Upsert Issue (ensure bidirectional links like timeTracking.issue=this)
        Issue entity = issueRepository.findByKey(issueKey).orElseGet(Issue::new);
        issueMapper.updateEntityFromResponse(entity, jr);
        entity.setKey(issueKey);
        if (entity.getTimeTracking() != null) {
            entity.getTimeTracking().setIssue(entity);
        }
        entity = issueRepository.saveAndFlush(entity);

        // 3) Pull comments from Jira and upsert them with the MANAGED Issue

        List<CommentResponse> jiraComments = jiraCommentService.getCommentsByIssueKey(issueKey);

        List<String> jiraCommentList = new ArrayList<>();
        for (CommentResponse comment : jiraComments) {
            jiraCommentList.add(comment.getId());
        }
        jiraCommentService.synchronizeCommentsByIdsList(issueKey, jiraCommentList);
    }


    private void upsertFromJira(IssueResponse src) {
        helperMethod.requireNonNull(src, "IssueResponse is required");
        helperMethod.requireNonBlank(src.getKey(), "IssueResponse.key is required");

        final String key = src.getKey();
        final IssueFields f = src.getFields();

        // 1) Load existing or create new
        Issue entity = issueRepository.findByKey(key).orElseGet(Issue::new);

        // 2) Seed essentials
        entity.setKey(key);
        entity.setJiraId(src.getId());
        entity.setSelf(src.getSelf());

        // Derive project key if missing
        if ((entity.getProjectKey() == null || entity.getProjectKey().isBlank()) && f != null) {
            if (key.contains("-")) {
                entity.setProjectKey(key.substring(0, key.indexOf('-')));
            } else if (f.getProject() != null) {
                entity.setProjectKey(f.getProject().getKey());
            }
        }

        // 3) Resolve & set IssueType FK (REQUIRED, NOT NULL)
        //    Prefer Jira id; fallback to name
        IssueType issueTypeEntity = null;
        if (f != null && f.getIssueTypeResponse() != null) {
            var it = f.getIssueTypeResponse();
            String jiraTypeIdStr = it.getId();                // usually a String id from Jira
            String jiraTypeName  = it.getName();

            issueTypeEntity = issueTypeService.findOrCreateBIdOrName(
                    (jiraTypeIdStr == null || jiraTypeIdStr.isBlank()) ? null : jiraTypeIdStr,
                    jiraTypeName
            );
            if (issueTypeEntity == null) {
                throw new IllegalStateException("Unable to resolve local IssueType from Jira payload (id="
                        + jiraTypeIdStr + ", name=" + jiraTypeName + ") for issue " + key);
            }
        } else {
            throw new IllegalStateException("Jira issue type missing in response for issue " + key);
        }

        // 4) Map rich fields from Jira → entity
        //    Do the mapping BEFORE wiring children, but AFTER we resolved issueType (we'll re-assign it)
        issueMapper.updateEntityFromResponse(entity, src);

        // 5) Enforce FK & child back-refs AFTER mapping (mapper may null them)
        entity.setIssueType(issueTypeEntity); // <— THIS prevents issue_type_id NULL

        if (entity.getTimeTracking() != null) {
            IssueTimeTracking tt = entity.getTimeTracking();
            tt.setIssue(entity); // ensure FK issue_id on child
            // Optional: drop empty timetracking rows to avoid null-FK inserts
            boolean hasData =
                    (tt.getTimeSpent() != null)
                            || (tt.getOriginalEstimate() != null && !tt.getOriginalEstimate().isBlank())
                            || (tt.getRemainingEstimate() != null && !tt.getRemainingEstimate().isBlank())
                            || (tt.getOriginalEstimateSeconds() != null)
                            || (tt.getRemainingEstimateSeconds() != null)
                            || (tt.getAiEstimateMinutes() != null);
            if (!hasData) entity.setTimeTracking(null);
        }

        // 6) Timestamps & sync flag
        if (entity.getCreatedAt() == null && f.getCreated() != null) {
            entity.setCreatedAt(f.getCreated().toLocalDateTime());
        }
        if (f.getUpdated() != null) {
            entity.setUpdatedAt(f.getUpdated().toLocalDateTime());
        } else if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(java.time.LocalDateTime.now());
        }
        if (entity.getSyncStatus() == null) {
            entity.setSyncStatus(SynchronizationStatus.SYNCED);
        }

        // 7) Persist
        issueRepository.save(entity);
    }







}