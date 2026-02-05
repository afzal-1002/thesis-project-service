package com.pw.edu.pl.master.thesis.issues.dto.issue.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChangelogListRequest {
    @JsonProperty("changelogIds")
    private List<Long> changelogIds;
}
