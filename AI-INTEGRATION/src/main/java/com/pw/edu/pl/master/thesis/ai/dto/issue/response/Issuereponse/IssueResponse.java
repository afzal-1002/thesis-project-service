package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse; // Use an appropriate package

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


// =================================================================================================
//  ROOT DTO: ISSUE RESPONSE
// =================================================================================================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder @ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueResponse {
    private String expand;
    private String id;
    private String self;
    private String key;
    private IssueFields fields;
}