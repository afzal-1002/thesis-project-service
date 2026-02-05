package com.pw.edu.pl.master.thesis.issues.dto.project.project_issue;//package com.pl.edu.wut.master.thesis.bug.dto.project.project_issue;
//
//
//import com.fasterxml.jackson.annotation.JsonAnyGetter;
//import com.fasterxml.jackson.annotation.JsonAnySetter;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.pl.edu.wut.master.thesis.bug.dto.project.ProjectSummary;
//import com.pl.edu.wut.master.thesis.bug.model.common.*;
//import com.pl.edu.wut.master.thesis.bug.model.common.PriorityEnum;
//import com.pl.edu.wut.master.thesis.bug.model.common.Resolution;
//import com.pl.edu.wut.master.thesis.bug.model.common.StatusCategory;
//import com.pl.edu.wut.master.thesis.bug.model.common.Version;
//import com.pl.edu.wut.master.thesis.bug.model.common.Watches;
//import com.pl.edu.wut.master.thesis.bug.model.component.ComponentSummary;
//import com.pl.edu.wut.master.thesis.bug.model.common.Description;
//import com.pl.edu.wut.master.thesis.bug.model.user.UserSummary;
//import lombok.Data;
//
//import java.time.LocalDate;
//import java.time.OffsetDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class IssueDetails {
//    // core metadata
//    // Standard fields
//    private IssueTypeResponse issuetype;
//    private Integer timespent;
//    private ProjectSummary project;
//    private List<Version> fixVersions;
//    private Integer aggregatetimespent;
//    private StatusCategory statusCategory;
//    private Resolution resolution;
//    private OffsetDateTime resolutionDate;
//    private Integer workratio;
//    private Watches watches;
//    private OffsetDateTime lastViewed;
//    private OffsetDateTime created;
//    private PriorityEnum priorityEnum;
//    private List<String> labels;
//    private Integer timeEstimate;
//    private Integer aggregateTimeOriginalEstimate;
//    private List<Version> versions;
//    private List<IssueLink> issuelinks;
//
//    private Integer aggregateTimeEstimate;
//    private OffsetDateTime statusCategoryChangedDate;
//
//    @JsonProperty("timeoriginalestimate")
//    private Integer timeOriginalEstimate;
//
//
//    private OffsetDateTime updated;
//    private StatusResponse statusResponse;
//    private List<ComponentSummary> components;
//    private Description description;
//    private TimeTrackingResponse timetracking;
//    private String summary;
//
//    private Parent parent;
//
//    private List<SubTask> subtasks;
//
//    private UserSummary assignee;
//    private UserSummary creator;
//    private UserSummary reporter;
//
//    private AggregateProgress aggregateprogress;
//    private String environment;
//    private LocalDate duedate;
//    private Progress progress;
//    private Votes votes;
//    private CommentWrapper comment;
//    private WorklogWrapper worklog;
//
//    @JsonProperty("attachment")
//    private List<Attachment> attachments;
//
//    @JsonAnySetter
//    private Map<String,Object> customFields = new HashMap<>();
//
//    @JsonAnyGetter
//    public Map<String,Object> getCustomFields() { return customFields; }
//
//    public <T> T getCustomField(String fieldName, Class<T> cls) {
//        Object raw = customFields.get(fieldName);
//        if (raw == null) return null;
//        return cls.cast(raw);
//    }
//}
//
//
