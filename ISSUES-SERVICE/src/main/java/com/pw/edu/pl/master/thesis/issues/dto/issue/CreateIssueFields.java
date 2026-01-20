package com.pw.edu.pl.master.thesis.issues.dto.issue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body;
import com.pw.edu.pl.master.thesis.issues.dto.project.ProjectReference;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeReference;
import com.pw.edu.pl.master.thesis.issues.dto.user.UserReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateIssueFields {

    private ProjectReference project;

    @JsonProperty("issuetype")
    private IssueTypeReference issuetype;

    private String              summary;
    private String              duedate;

    private UserReference assignee;
    private Body description;

    private List<String>        labels;


}
