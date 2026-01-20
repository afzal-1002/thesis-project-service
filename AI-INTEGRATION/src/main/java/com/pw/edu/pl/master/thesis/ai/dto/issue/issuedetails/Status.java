package com.pw.edu.pl.master.thesis.ai.dto.issue.issuedetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Status {

    private String id;
    private String name;
    private String description;

    @JsonProperty("statusCategory")
    private StatusCategory statusCategory;

    static class StatusCategory {

        private String key;   // new | indeterminate | done
        private String name;  // To Do | In Progress | Done
    }
}
