package com.pw.edu.pl.master.thesis.user.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pw.edu.pl.master.thesis.user.dto.AvatarUrls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data  @NoArgsConstructor
@AllArgsConstructor @Builder
public class JiraUserResponse {
    private String self;
    private String accountId;
    private String accountType;
    private String emailAddress;
    private AvatarUrls avatarUrls;
    private String displayName;
    private boolean active;
    private String timeZone;
    private String locale;
    private Groups groups;
    private ApplicationRoles applicationRoles;
    private String expand;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data  @NoArgsConstructor
    @AllArgsConstructor @Builder
    public static class Groups {
        private int size;
        private List<Object> items = new ArrayList<>();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data  @NoArgsConstructor
    @AllArgsConstructor @Builder
    public static class ApplicationRoles {
        private int size;
        private List<Object> items = new ArrayList<>();

    }

}
