package com.pw.edu.pl.master.thesis.user.dto.project;



import com.pw.edu.pl.master.thesis.user.dto.AvatarUrls;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import lombok.*;

import java.util.List;
import java.util.Map;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraProjectResponse {
   private String expand;
   private String self;
   private String id;
    private String key;
    private String description;
    private UserSummary lead;

//    private  List<ComponentSummary> components;

    private  List<IssueType> issueTypes;
    private  String assigneeType;
    private  List<Version> versions;
    private  String name;
    private  Map<String, String> roles;
    private  String projectTypeKey;
    private String projectTemplateKey;
    private boolean simplified;
    private String style;
    private boolean isPrivate;
    private Map<String, Object> properties;

    private AvatarUrls avatarUrls;

}
