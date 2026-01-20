package com.pw.edu.pl.master.thesis.ai.client.issue;

import com.pw.edu.pl.master.thesis.ai.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.ai.dto.appuser.AuthUserDTO;
import com.pw.edu.pl.master.thesis.ai.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.IssueResponse;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(
//        name = "issue-client",
//        url = "${issues.service.base-url}",
//        path = "/api/wut/issues",
//        configuration = FeignSecurityConfiguration.class
//)



@FeignClient(
        name = "ISSUES-SERVICE",
        contextId = "IssueClient",
        path = "/api/wut/issues",
        configuration = FeignSecurityConfiguration.class
)
public interface IssueClient {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    IssueResponse createIssue(@RequestBody CreateIssueRequest request);

    @GetMapping("/authUser")
    AuthUserDTO getAuthUser();

    @GetMapping("/users")
    List<UserSummary> getAllUsersViaUserService();

    @GetMapping("/{key}")
    IssueResponse getIssueByKey(@PathVariable("key") String issueKey);

    @PostMapping("/projects/sync/issues")
    List<IssueResponse> synchronizeProjectIssues(@RequestParam("projectKey") String projectKey);

    @PutMapping("/{key}")
    IssueResponse updateIssue(@PathVariable("key") String issueKey,
                              @RequestBody CreateIssueRequest request);

    @GetMapping("/projects/{projectKey}/issues")
    List<IssueResponse> listIssuesForProjectKey(@PathVariable("projectKey") String projectKey);

    @GetMapping("/projects/{projectKey}/issues/local")
    List<IssueResponse> listIssueResponsesByProjectId(@PathVariable("projectKey") String projectKey);

    @PostMapping("/{key}/sync")
    IssueResponse synchronizeIssueByKey(@PathVariable("key") String issueKey);

    @PostMapping("/{key}/sync-detached")
    void syncIssueByIssueKey(@PathVariable("key") String issueKey);
}