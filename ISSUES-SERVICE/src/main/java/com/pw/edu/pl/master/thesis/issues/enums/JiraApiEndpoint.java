package com.pw.edu.pl.master.thesis.issues.enums;

import lombok.Getter;

@Getter
public enum JiraApiEndpoint {
    // Me / Users / StatusResponse / Search
    ME("/myself"),
    USER("/user"),
    USER_SEARCH("/user/search"),
    STATUS("/statusResponse"),
    SEARCH("/search"),
    ISSUE_BY_ID_OR_KEY("/issue/%s"),
    // Issues
    ISSUE("/issue"),
    ISSUE_ASSIGNEE("/issue/%s/assignee"),
    ISSUE_CHANGELOG("/issue/%s/changelog"),
    ISSUE_CHANGELOG_LIST("/issue/%s/changelog/list"),
    ISSUE_ARCHIVE("/issue/archive"),
    ISSUE_UNARCHIVE("/issue/unarchive"),
    ISSUE_CREATEMETA("/issue/createmeta"),
    ISSUE_CREATEMETA_ISSUETYPES("/issue/createmeta/%s/issuetypes"),


    // ISSUE COMMENT
    ISSUE_COMMENTS("/issue/%s/comment"),
    ISSUE_COMMENT_BY_ID("/issue/%s/comment/%s"),

    // Issue types
    ISSUE_TYPE("/issuetype"),
    ISSUE_TYPE_PROJECT("/issuetype/project"),

    // Comments
    COMMENT("/comment"),
    COMMENT_ASSIGNEE("/comment/assignee"),
    COMMENT_LIST("/comment/list"),
    COMMENT_CREATEMETA("/comment/createmeta"),

    SEARCH_JQL("/search/jql"),

    // Fields
    FIELD("/field"),

    // Projects
    PROJECT("/project"),
    PROJECT_ID_OR_KEY("/project/%s"),
    PROJECT_SEARCH("/project/search"),
    PROJECT_STATUSES("/project/%s/statuses"),
    PROJECT_VALIDATE_KEY("/projectvalidate/key"),
    PROJECT_VALIDATE_VALID_KEY("/projectvalidate/validProjectKey"),
    PROJECT_VALIDATE_VALID_NAME("/projectvalidate/validProjectName");




    private final String path;
    JiraApiEndpoint(String path) { this.path = path; }

    public String buildUrl(String baseUrl, String version) {
        return baseUrl + "/rest/api/" + version + this.path;
    }

    public static final String PROJECT_EXPAND = String.join(",",
            "lead","issueTypes","components","roles","versions",
            "description","insight","projectKeys","url","permissions"
    );


}
