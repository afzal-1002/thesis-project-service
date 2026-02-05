package com.pw.edu.pl.master.thesis.user.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueType {

    private String   self;
    @Column(columnDefinition = "TEXT")
    private String   description;
    private String   iconUrl;
    @Column(nullable = false)
    private String   name;
    private String   untranslatedName;
    private boolean  subtask;
    private Integer  avatarId;
    private int      hierarchyLevel;

    @Column(nullable = false)
    @JsonIgnore
    private String   projectKey;

}
