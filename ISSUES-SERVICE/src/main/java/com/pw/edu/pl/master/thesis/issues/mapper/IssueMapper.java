package com.pw.edu.pl.master.thesis.issues.mapper;

import com.pw.edu.pl.master.thesis.issues.dto.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.IssueResponseSummary;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.*;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeReference;
import com.pw.edu.pl.master.thesis.issues.dto.project.ProjectReference;
import com.pw.edu.pl.master.thesis.issues.dto.user.UserReference;
import com.pw.edu.pl.master.thesis.issues.enums.PriorityEnum;
import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import com.pw.edu.pl.master.thesis.issues.model.issue.IssueTimeTracking;
import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueFields;
import com.pw.edu.pl.master.thesis.issues.model.status.Status;
import com.pw.edu.pl.master.thesis.issues.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IssueMapper {

    private final StatusService statusService;
    private final HelperMethod helperMethod;

    public Issue toIssueEntity(IssueResponse response) {
        if (response == null || response.getFields() == null) {
            return null;
        }

        IssueFields fields = response.getFields();

        // 1. Build the base Issue entity
        Issue.IssueBuilder builder = Issue.builder();

        // Root DTO fields
        builder.jiraId(response.getId());
        builder.key(response.getKey());
        builder.self(response.getSelf());

        // Extract Project key and ID
        if (response.getKey() != null && response.getKey().contains("-")) {
            builder.projectKey(response.getKey().substring(0, response.getKey().indexOf('-')));
        } else if (fields.getProject() != null) {
            builder.projectKey(fields.getProject().getKey());
        }

        // IssueFields primitive fields
        builder.description(flattenDescription(fields.getDescription()));

        // Dates
        builder.createdAt(convertToLocalDateTime(fields.getCreated()));
        builder.updatedAt(convertToLocalDateTime(fields.getUpdated()));
        builder.resolvedAt(convertToLocalDateTime(fields.getResolutionDate()));
        builder.dueDate(fields.getDueDate());

        mapAssigneeReporterCreator(fields, builder);
        mapStatusEntity(fields, builder);
        mapIssueTypeEntity(fields, builder);
        mapTimeTracking(fields, builder);

        // Final statusResponse
        builder.syncStatus(SynchronizationStatus.SYNCED);

        return builder.build();
    }

    /* ------------------------------ Entity → API (Mapping DB Entity to Jira DTO) ------------------------------ */

    public IssueResponse toIssueResponse(Issue issue) {
        if (issue == null) return null;

        IssueFields fields = new IssueFields();
        fields.setDescription(null); // ADF conversion is complex, stubbed for now

        fields.setCreated(convertToOffsetDateTime(issue.getCreatedAt()));
        fields.setUpdated(convertToOffsetDateTime(issue.getUpdatedAt()));
        fields.setResolutionDate(convertToOffsetDateTime(issue.getResolvedAt()));

        return IssueResponse.builder()
                .id(issue.getJiraId())
                .key(issue.getKey())
                .self(issue.getSelf())
                .fields(fields)
                .build();
    }

    public IssueResponseSummary toIssueResponseSummary(IssueResponse response) {
        if (response == null) return null;
        return IssueResponseSummary.builder()
                .expand(response.getExpand())
                .id(response.getId())
                .key(response.getKey())
                .self(response.getSelf())
                .build();
    }

    public void copyInto(Issue target, IssueResponse source) {
        if (target == null || source == null || source.getFields() == null) return;

        var fields = source.getFields();

        // 1. Create a new IssueFields object using the builder with all fields copied
        IssueFields newFields = IssueFields.builder()
                .summary(fields.getSummary())
                .description(fields.getDescription())
                .statusResponse(fields.getStatusResponse())
                .priorityResponse(fields.getPriorityResponse())
                .issueTypeResponse(fields.getIssueTypeResponse())

                // Time Estimation/Tracking
                .issueTimeTrackingResponse(fields.getIssueTimeTrackingResponse())
                .timeSpent(fields.getTimeSpent())
                .timeEstimate(fields.getTimeEstimate())
                .timeOriginalEstimate(fields.getTimeOriginalEstimate())
                .aggregateTimeSpent(fields.getAggregateTimeSpent())
                .aggregateTimeEstimate(fields.getAggregateTimeEstimate())
                .aggregateTimeOriginalEstimate(fields.getAggregateTimeOriginalEstimate())

                // Project, Resolution, Dates
                .project(fields.getProject())
                .resolution(fields.getResolution())
                .resolutionDate(fields.getResolutionDate())
                .created(fields.getCreated())
                .updated(fields.getUpdated())
                .dueDate(fields.getDueDate())
                .statusCategory(fields.getStatusCategory())
                .statusCategoryChangedDate(fields.getStatusCategoryChangedDate())

                // People
                .assignee(fields.getAssignee())
                .reporter(fields.getReporter())
                .creator(fields.getCreator())

                // Structure/Relations
                .parent(fields.getParent())
                .subtasks(fields.getSubtasks())
                .issueLinks(fields.getIssueLinks())
                .components(fields.getComponents())

                // Versions/Labels
                .fixVersions(fields.getFixVersions())
                .versions(fields.getVersions())
                .labels(fields.getLabels())

                // Progress/Metrics
                .workRatio(fields.getWorkRatio())
                .aggregateprogress(fields.getAggregateprogress())
                .progress(fields.getProgress())
                .votes(fields.getVotes())
                .watches(fields.getWatches())
                .lastViewed(fields.getLastViewed())

                // Content/Attachments
                .environment(fields.getEnvironment())
                .comment(fields.getComment())
                .worklog(fields.getWorklog())
                .attachment(fields.getAttachment())

                // Custom Fields
                .customFields(fields.getCustomFields())
                .build();

        // 2. Update target entity fields using the copied data (newFields)

        target.setSelf(nullSafe(source.getSelf(), target.getSelf()));

        // Update key fields only if they exist in the source
        target.setKey(nullSafe(source.getKey(), target.getKey()));
        target.setJiraId(nullSafe(source.getId(), target.getJiraId()));

        String flattened = flattenDescription(newFields.getDescription());
        target.setDescription(nullSafe(flattened, target.getDescription()));

        if (source.getKey() != null) {
            String projectKey = source.getKey().contains("-") ?
                    source.getKey().substring(0, source.getKey().indexOf('-')) : target.getProjectKey();
            target.setProjectKey(projectKey);
        }

        // Timestamps
        target.setCreatedAt(convertToLocalDateTime(newFields.getCreated()));
        target.setUpdatedAt(convertToLocalDateTime(newFields.getUpdated()));
        target.setResolvedAt(convertToLocalDateTime(newFields.getResolutionDate()));
        target.setDueDate(newFields.getDueDate());

        // -----------------------------------------------------------
        // FIX: Using 'update...' methods and passing the 'target' entity
        // -----------------------------------------------------------
        updateStatusEntity(newFields, target);
        updateIssueTypeEntity(newFields, target);
        updateAssigneeReporterCreator(newFields, target);
        updateTimeTracking(newFields, target);
    }

    /* ========================================================================
     * NEW: Build CreateIssueRequest (Jira-create compatible) from IssueResponse
     * ======================================================================== */
    public CreateIssueRequest toCreateIssueRequest(IssueResponse src) {
        if (src == null) return emptyCreateRequest();

        IssueFields f = src.getFields();
        if (f == null) return emptyCreateRequest();

        CreateIssueFields fields = CreateIssueFields.builder()
                .project(toProjectRef(f.getProject()))
                .issuetype(toIssueTypeRef(f.getIssueTypeResponse()))
                .summary(helperMethod.safe(f.getSummary()))
                .duedate(toIsoDate(f.getDueDate()))
                .assignee(toUserRef(f.getAssignee()))
                .description(f.getDescription())
                .labels(safeList(f.getLabels()))
                .build();

        return CreateIssueRequest.builder().fields(fields).build();
    }

    private CreateIssueRequest emptyCreateRequest() {
        return CreateIssueRequest.builder()
                .fields(CreateIssueFields.builder().build())
                .build();
    }

    private ProjectReference toProjectRef(ProjectSummary p) {
        if (p == null) return null;
        Long id = null;
        try {
            Object raw = p.getId(); // id might be String/Number depending on your DTO
            if (raw instanceof String s && s.matches("\\d+")) id = Long.parseLong(s);
        } catch (Exception ignored) {}
        String key = helperMethod.safe(p.getKey());
        if (id == null && !helperMethod.hasText(key)) return null;
        return ProjectReference.of(id, key);
    }

    private IssueTypeReference toIssueTypeRef(IssueTypeResponse it) {
        if (it == null) return null;
        Long id = null;
        try {
            Object raw = it.getId(); // id might be String/Number
            if (raw instanceof String s && s.matches("\\d+")) id = Long.parseLong(s);
        } catch (Exception ignored) {}
        String name = helperMethod.safe(it.getName());
        if (id == null && !helperMethod.hasText(name)) return null;
        return IssueTypeReference.of(id, name);
    }

    private UserReference toUserRef(UserSummary u) {
        if (u == null) return null;
        String id = helperMethod.safe(u.getAccountId()); // Jira Cloud canonical identifier
        String username =helperMethod.hasText(u.getDisplayName()) ? u.getDisplayName().trim() : null;
        if (!helperMethod.hasText(id) && !helperMethod.hasText(username)) return null;
        return UserReference.of(id, username);
    }

    private String toIsoDate(LocalDate d) {
        return d == null ? null : d.toString(); // yyyy-MM-dd
    }

    private List<String> safeList(List<String> in) {
        if (in == null || in.isEmpty()) return null;
        return in.stream().filter(helperMethod::hasText).map(String::trim).distinct().toList();
    }

    /* ------------------------------ Helper Methods ------------------------------ */

    /** Maps Assignee, Reporter, and Creator IDs to the Issue entity. */
    private void mapAssigneeReporterCreator(IssueFields fields, Issue.IssueBuilder builder) {
        if (fields.getAssignee() != null) {
            builder.assigneeId(fields.getAssignee().getAccountId());
        }
        if (fields.getReporter() != null) {
            builder.reporterId(fields.getReporter().getAccountId());
        }
        // Creator ID not in entity, so skipped
    }

    /** Creates and sets the IssueTimeTracking sub-entity. */
    private void mapTimeTracking(IssueFields fields, Issue.IssueBuilder builder) {
        TimeTrackingResponse ttDto = fields.getIssueTimeTrackingResponse();
        if (ttDto == null) return;

        IssueTimeTracking timeTrackingEntity = IssueTimeTracking.builder()
                .originalEstimateSeconds(ttDto.getOriginalEstimateSeconds())
                .remainingEstimateSeconds(ttDto.getRemainingEstimateSeconds())
                .build();

        builder.timeTracking(timeTrackingEntity);
    }

    /** Maps the IssueFields statusResponse DTO to the Issue entity's statusResponse. */
    private void mapStatusEntity(IssueFields fields, Issue.IssueBuilder builder) {
        if (fields == null || fields.getStatusResponse() == null) return;
        String statusId = fields.getStatusResponse().getId();
        if (statusId == null) return;
        Long id = safeParseLong(statusId);
        if (id == null) return;

        // Load Status entity and set it if your Issue has a Status relation (builder method omitted in your snippet)
        Status entity = statusService.getStatusById(String.valueOf(id));
        // e.g., builder.status(entity);  // add this if your Issue has a Status field in the builder
    }

    /** Maps the IssueFields issue type DTO to the Issue entity's issueType. */
    private void mapIssueTypeEntity(IssueFields fields, Issue.IssueBuilder builder) {
        if (fields == null || fields.getIssueTypeResponse() == null) return;
        String issueTypeId = fields.getIssueTypeResponse().getId();
        if (issueTypeId == null) return;
        // If you keep an IssueType entity, resolve and set it here via a service/rep (not shown in your snippet)
        // e.g., builder.issueType(issueTypeEntity);
    }

    /** Converts DTO PriorityResponse to Entity PriorityEnum. */
    private PriorityEnum convertPriorityDtoToEnum(PriorityResponse priorityResponse) {
        if (priorityResponse == null || priorityResponse.getName() == null) return null;
        try {
            return PriorityEnum.valueOf(priorityResponse.getName().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Converts Entity PriorityEnum to DTO PriorityResponse. */
    private PriorityResponse convertEnumToPriorityDto(PriorityEnum priorityEnum) {
        if (priorityEnum == null) return null;
        return PriorityResponse.builder().name(priorityEnum.name()).build();
    }

    /** Use Body.extractTextFromBody (recursive) and join with newlines. (Stubbed) */
    private static String flattenDescription(Body description) {
        // This method depends on the concrete implementation of the Body DTO class
        return null;
    }

    private static LocalDateTime convertToLocalDateTime(OffsetDateTime odt) {
        return odt != null ? odt.toLocalDateTime() : null;
    }

    private static OffsetDateTime convertToOffsetDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.atOffset(OffsetDateTime.now().getOffset()) : null;
    }

    private static Long safeParseLong(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static <T> T nullSafe(T newVal, T current) {
        return newVal != null ? newVal : current;
    }

    /* ------------------------------ Helper Methods (Updating Existing Issue - Missing Methods) ------------------------------ */

    /** Updates the Status entity reference on the existing Issue entity. */
    private void updateStatusEntity(IssueFields fields, Issue target) {
        if (fields.getStatusResponse() == null) return;
        String statusId = fields.getStatusResponse().getId();
        if (statusId == null) return;
        Long id = safeParseLong(statusId);
        if (id == null) return;

        Status entity = statusService.getStatusById(String.valueOf(id));
        // target.setStatus(entity); // Uncomment if your Issue has a Status relation and setter
    }

    /** Updates the IssueType entity reference on the existing Issue entity. */
    private void updateIssueTypeEntity(IssueFields fields, Issue target) {
        if (fields.getIssueTypeResponse() == null) return;
        String issueTypeId = fields.getIssueTypeResponse().getId();
        if (issueTypeId == null) return;
    }

    /** Updates Assignee and Reporter IDs on the existing Issue entity. */
    private void updateAssigneeReporterCreator(IssueFields fields, Issue target) {
        if (fields.getAssignee() != null) {
            target.setAssigneeId(fields.getAssignee().getAccountId());
        }
        if (fields.getReporter() != null) {
            target.setReporterId(fields.getReporter().getAccountId());
        }
    }

    /** Updates the IssueTimeTracking sub-entity on the existing Issue entity. */
    private void updateTimeTracking(IssueFields fields, Issue target) {
        TimeTrackingResponse ttDto = fields.getIssueTimeTrackingResponse();
        if (ttDto == null) return;

        if (target.getTimeTracking() == null) {
            target.setTimeTracking(IssueTimeTracking.builder().build());
        }

        target.getTimeTracking().setOriginalEstimateSeconds(ttDto.getOriginalEstimateSeconds());
        target.getTimeTracking().setRemainingEstimateSeconds(ttDto.getRemainingEstimateSeconds());
    }

    public void updateEntityFromResponse(Issue target, IssueResponse source) {
        if (target == null || source == null) return;

        // ---- Root fields (id/key/self) ----
        target.setJiraId(nullSafe(source.getId(), target.getJiraId()));
        target.setSelf(nullSafe(source.getSelf(), target.getSelf()));
        target.setKey(nullSafe(source.getKey(), target.getKey()));

        // ---- Derive/ensure projectKey ----
        if (source.getKey() != null && source.getKey().contains("-")) {
            String projectKey = source.getKey().substring(0, source.getKey().indexOf('-'));
            target.setProjectKey(nullSafe(projectKey, target.getProjectKey()));
        } else if (source.getFields() != null && source.getFields().getProject() != null) {
            target.setProjectKey(nullSafe(source.getFields().getProject().getKey(), target.getProjectKey()));
        }

        // Nothing more to do if fields are missing
        IssueFields fields = source.getFields();
        if (fields == null) return;

        // ---- Description (ADF -> flat text) ----
        String flat = flattenDescription(fields.getDescription());
        target.setDescription(nullSafe(flat, target.getDescription()));

        // ---- Timestamps ----
        if (fields.getIssueTimeTrackingResponse() != null) {
            IssueTimeTracking tt = target.getTimeTracking();
            if (tt == null) {
                tt = IssueTimeTracking.builder().build();
            }
            tt.setOriginalEstimateSeconds(fields.getIssueTimeTrackingResponse().getOriginalEstimateSeconds());
            tt.setRemainingEstimateSeconds(fields.getIssueTimeTrackingResponse().getRemainingEstimateSeconds());
            // set via helper to ensure backref
            target.setTimeTracking(tt);
        }

        target.setCreatedAt(convertToLocalDateTime(fields.getCreated()));
        target.setUpdatedAt(convertToLocalDateTime(fields.getUpdated()));
        target.setResolvedAt(convertToLocalDateTime(fields.getResolutionDate()));
        target.setDueDate(fields.getDueDate());

        // ---- Status, IssueType, People, TimeTracking (delegate to helpers) ----
        updateStatusEntity(fields, target);
        updateIssueTypeEntity(fields, target);
        updateAssigneeReporterCreator(fields, target);
        updateTimeTracking(fields, target);

        // If you keep a sync flag on Issue, ensure it's set after successful upsert
        if (target.getSyncStatus() == null) {
            target.setSyncStatus(SynchronizationStatus.SYNCED);
        }
    }



    /* ------------------------------ local helpers for mapper ------------------------------ */




}
