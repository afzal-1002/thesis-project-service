//package com.ii.pw.edu.pl.master.thesis.project.client;
//
//
//import com.ii.pw.edu.pl.master.thesis.project.dto.issue.IssueSummary;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@FeignClient(name = "issue-service", url = "${clients.issue.base-url}")
//public interface IssueClient {
//    @PostMapping("/internal/issues/_batch-by-keys")
//    Map<String, IssueSummary> findIssuesByKeys(@RequestBody Set<String> keys);
//
//    @GetMapping("/internal/issues/_by-project-key")
//    List<IssueSummary> findByProjectKey(@RequestParam String projectKey,
//                                        @RequestParam(defaultValue = "0") int page,
//                                        @RequestParam(defaultValue = "50") int size);
//}
//
