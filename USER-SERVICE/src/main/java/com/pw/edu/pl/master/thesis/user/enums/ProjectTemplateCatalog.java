package com.pw.edu.pl.master.thesis.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectTemplateCatalog {

    // ── Software
    SOFTWARE_SCRUM(
            "software",
            "com.pyxis.greenhopper.jira:gh-scrum-template",
            "Scrum Software Development",
            "Scrum board, sprints"
    ),
    SOFTWARE_KANBAN(
            "software",
            "com.pyxis.greenhopper.jira:gh-kanban-template",
            "Kanban Software Development",
            "Kanban board, WIP limits"
    ),
    SOFTWARE_BASIC(
            "software",
            "com.pyxis.greenhopper.jira:gh-basic-template",
            "Basic Software Development",
            "Simple software project"
    ),

    // ── Business (Jira Work Management)
    BUSINESS_PROJECT_MANAGEMENT(
            "business",
            "com.atlassian.jira-core-project-templates:jira-core-simplified-project-management",
            "Project Management",
            "Tasks, timelines"
    ),
    BUSINESS_TASK_TRACKING(
            "business",
            "com.atlassian.jira-core-project-templates:jira-core-simplified-task-tracking",
            "Task Tracking",
            "Simple task board"
    ),
    BUSINESS_PROCESS_MANAGEMENT(
            "business",
            "com.atlassian.jira-core-project-templates:jira-core-simplified-process-management",
            "Process Management",
            "Workflows for processes"
    ),

    // ── Service Management
    SERVICE_ITSM(
            "service_desk",
            "com.atlassian.servicedesk:simplified-it-service-desk",
            "IT Service Management",
            "ITSM request types, queues, portal"
    ),
    SERVICE_CUSTOMER(
            "service_desk",
            "com.atlassian.servicedesk:simplified-customer-service-desk",
            "Customer Service Management",
            "CSM request types, portal"
    ),
    SERVICE_GENERAL(
            "service_desk",
            "com.atlassian.servicedesk:simplified-general-service-desk",
            "General Service Desk",
            "General helpdesk template"
    );

    private final String projectTypeKey;      // software | business | service_desk
    private final String projectTemplateKey;  // the key you pass to Jira create project
    private final String displayName;         // human-friendly name
    private final String description;         // optional details
}
