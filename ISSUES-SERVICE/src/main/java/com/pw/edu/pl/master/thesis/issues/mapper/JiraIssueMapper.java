package com.pw.edu.pl.master.thesis.issues.mapper;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueFields;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.ProjectSummary;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import com.pw.edu.pl.master.thesis.issues.dto.project.ProjectResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.IssueResponseSummary;

import com.pw.edu.pl.master.thesis.issues.service.IssueService;
import com.pw.edu.pl.master.thesis.issues.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class JiraIssueMapper {

    private final JiraIssueService jiraIssueService;
    private final IssueService issueService;

    public IssueResponse fromNewIssueToIssueResponse(IssueResponseSummary jira) {
        if (jira == null || jira.getKey() == null) {
            throw new IllegalArgumentException("Cannot map null IssueResponse");
        }
        IssueResponse fetchedFromJira = jiraIssueService.getIssueByKeyJira(jira.getKey());
        return buildIssueResponse(jira.getId(), jira.getKey(), jira.getSelf(), fetchedFromJira.getFields());
    }

    public IssueResponse toIssueResponse(IssueResponseSummary issueResponse) {
        if (issueResponse == null || issueResponse.getKey() == null) {
            throw new IllegalArgumentException("Cannot map null IssueResponse");
        }
        IssueResponse fetchedFromLocal = issueService.getIssueByKey(issueResponse.getKey());
        return buildIssueResponse(issueResponse.getId(), issueResponse.getKey(), issueResponse.getSelf(), fetchedFromLocal.getFields());
    }

    private IssueResponse buildIssueResponse(String id, String key, String self, IssueFields fields) {
        if (fields == null) { fields = new IssueFields(); }

        IssueFields newFields = IssueFields.builder()
                .summary(fields.getSummary())
                .description(fields.getDescription())

                // StatusResponse and PriorityResponse
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
                .dueDate(fields.getDueDate()) // Uses Java property: dueDate
                .statusCategory(fields.getStatusCategory())
                .statusCategoryChangedDate(fields.getStatusCategoryChangedDate())

                // People
                .assignee(fields.getAssignee())
                .reporter(fields.getReporter())
                .creator(fields.getCreator())

                // Structure/Relations
                .parent(fields.getParent())
                .subtasks(fields.getSubtasks())
                .issueLinks(fields.getIssueLinks()) // Uses Java property: issueLinks
                .components(fields.getComponents())

                // Versions/Labels
                .fixVersions(fields.getFixVersions())
                .versions(fields.getVersions())
                .labels(fields.getLabels())

                // Progress/Metrics
                .workRatio(fields.getWorkRatio()) // Uses Java property: workRatio
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

                // Custom Fields (must be last)
                .customFields(fields.getCustomFields())
                .build();

        return IssueResponse.builder()
                .id(id)
                .key(key)
                .self(self)
                .fields(newFields)
                .build();
    }



    private ProjectResponse mapProject(ProjectSummary project) {
        if (project == null) { return null; }
        ProjectResponse.ProjectResponseBuilder builder = ProjectResponse.builder();
        builder.id(project.getId() != null ? Long.valueOf(project.getId()) : null);
        builder.key(project.getKey());
        builder.name(project.getName());
        return builder.build();
    }

    private String getAccountId(UserSummary user) {
        return user != null ? user.getAccountId() : null;
    }
}
