package com.pw.edu.pl.master.thesis.ai.dto.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class JiraUserMeResponse {
    private String self;
    private String accountId;
    private String accountType;
    private String emailAddress;
    private Map<String, String> avatarUrls;  // e.g. "48x48" -> URL
    private String displayName;
    private boolean active;
    private String timeZone;
    private String locale;
    private ExpandableList groups;
    private ExpandableList applicationRoles;
    private String expand;

    @Data
    public static class ExpandableList {
        private int size;
        private List<Object> items; // keep it generic; Jira often returns empty list here
    }
}
