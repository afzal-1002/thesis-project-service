package com.pw.edu.pl.master.thesis.ai.client.issue;

import com.pw.edu.pl.master.thesis.ai.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.ai.dto.issue.issuedetails.IssueDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(
//        name = "issue-details",
//        url = "${issues.service.base-url}",
//        path = "/api/wut/issues",
//        configuration = FeignSecurityConfiguration.class
//)


@FeignClient(
        name = "ISSUES-SERVICE",
        contextId = "IssueDetailsClient",
        path = "/api/wut/issues",
        configuration = FeignSecurityConfiguration.class
)
public interface IssueDetailsClient {

    @GetMapping("/{issueKey}/details")
    IssueDetails getIssueDetails(@PathVariable("issueKey") String issueKey);

    @GetMapping(value = "/{issueKey}/details.txt", produces = "text/plain; charset=UTF-8")
    String getIssueDetailsPlain(@PathVariable("issueKey") String issueKey);
}

