package com.pw.edu.pl.master.thesis.issues.dto.project;



import com.pw.edu.pl.master.thesis.issues.dto.common.AvatarUrls;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import com.pw.edu.pl.master.thesis.issues.model.issuetype.IssueType;
import lombok.*;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProjectDetailsSummary {
    private String expand;
    private String self;
    private String id;
    private String key;
    private String description;
    private UserSummary lead;
//    private List<ComponentSummary> components;
    private List<IssueType> issueTypes;
    private String assigneeType;
    private List<Version> versions;
    private String name;
    private Map<String, String> roles;
    private AvatarUrls avatarUrls;
    private String projectTypeKey;
    private boolean simplified;
    private String style;
    private boolean isPrivate;
    private Map<String, Object> properties;
}
