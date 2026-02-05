package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter @ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueFields {

    // ───────────────── core/standard ─────────────────
    @JsonProperty("issuetype")             // <-- add this
    private IssueTypeResponse issueTypeResponse;

    @JsonProperty("timespent")
    private Integer timeSpent;

    private ProjectSummary project;

    private List<Version> fixVersions;

    @JsonProperty("aggregatetimespent")
    private Integer aggregateTimeSpent;

    private StatusCategory statusCategory;
    private Resolution resolution;

    @JsonProperty("resolutiondate")
    private OffsetDateTime resolutionDate;

    @JsonProperty("workratio")
    private Integer workRatio;

    private Watches watches;

    private OffsetDateTime lastViewed;
    private OffsetDateTime created;

    private PriorityResponse priorityResponse;
    private List<String> labels;

    @JsonProperty("timeestimate")
    private Integer timeEstimate;

    @JsonProperty("aggregatetimeoriginalestimate")
    private Integer aggregateTimeOriginalEstimate;

    private List<Version> versions;

    @JsonProperty("issuelinks")
    private List<IssueLink> issueLinks;

    @JsonProperty("aggregatetimeestimate")
    private Integer aggregateTimeEstimate;

    @JsonProperty("statuscategorychangedate")
    private OffsetDateTime statusCategoryChangedDate;

    @JsonProperty("timeoriginalestimate")
    private Integer timeOriginalEstimate;

    private OffsetDateTime updated;

    private StatusResponse statusResponse;

    private List<ComponentSummary> components;

    private Body description;

    @JsonProperty("timetracking")
    private TimeTrackingResponse issueTimeTrackingResponse;

    private String summary;

    private ParentIssue parent;

    private List<SubTask> subtasks;

    private UserSummary assignee;
    private UserSummary creator;
    private UserSummary reporter;

    private AggregateProgress aggregateprogress;

    private String environment;

    @JsonProperty("duedate")
    private LocalDate dueDate;

    private Progress progress;
    private Votes votes;

    private CommentWrapper comment;
    private WorklogWrapper worklog;

    @JsonProperty("attachment")
    private List<Attachment> attachments;

    private Map<String, Object> customFields = new HashMap<>();

    @JsonAnySetter
    public void putCustomField(String key, Object value) {
        customFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getCustomFields() {
        return customFields;
    }
}

