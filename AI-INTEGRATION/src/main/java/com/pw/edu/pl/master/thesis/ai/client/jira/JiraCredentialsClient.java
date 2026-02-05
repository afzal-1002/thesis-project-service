package com.pw.edu.pl.master.thesis.ai.client.jira;

import com.pw.edu.pl.master.thesis.ai.configuration.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


//@FeignClient( name = "comment-service",
//        contextId = "JiraCommentService",
//        url = "${clients.jira.base-url}",
//        path = "/api/wut/credentials",
//        configuration = FeignSecurityConfiguration.class
//)


@FeignClient(
        name = "ISSUES-SERVICE",
        contextId = "JiraCredentialsClient",
        path = "/api/wut/credentials",
        configuration = FeignSecurityConfiguration.class
)
public interface JiraCredentialsClient {
    @PostMapping("/rest/api/3/issue/{issueKey}/comment")
    Map<String, Object> addComment(@PathVariable("issueKey") String issueKey,
                                   @RequestBody Map<String, Object> body);
}
