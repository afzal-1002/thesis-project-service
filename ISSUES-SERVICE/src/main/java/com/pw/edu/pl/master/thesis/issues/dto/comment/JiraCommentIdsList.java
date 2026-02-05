package com.pw.edu.pl.master.thesis.issues.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraCommentIdsList {
    @JsonProperty("ids")
    private List<String> ids;
}
