package com.pw.edu.pl.master.thesis.ai.dto.issuetype;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueTypeReference {
    private Long id;
    private String name;

    public static IssueTypeReference of(Long id, String name) {
        return new IssueTypeReference(id, name);
    }

}