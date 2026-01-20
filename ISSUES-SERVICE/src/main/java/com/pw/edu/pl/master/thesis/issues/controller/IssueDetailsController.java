package com.pw.edu.pl.master.thesis.issues.controller;

import com.pw.edu.pl.master.thesis.issues.dto.issue.issuedetails.IssueDetails;
import com.pw.edu.pl.master.thesis.issues.service.IssueDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/wut/issues")
@RequiredArgsConstructor
public class IssueDetailsController {

    private final IssueDetailsService issueDetailsService;

    /** JSON: compact details (title, description, hasAttachment, comments[].) */
    @GetMapping("/{issueKey}/details")
    public ResponseEntity<IssueDetails> getIssueDetails(@PathVariable String issueKey) {
        return ResponseEntity.ok(issueDetailsService.getIssueDetails(issueKey));
    }

    /** Plain text (uses IssueDetails.toString()). Nice for Postman or quick reads. */
    @GetMapping(value = "/{issueKey}/details.txt", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getIssueDetailsPlain(@PathVariable String issueKey) {
        return ResponseEntity.ok(issueDetailsService.getIssueDetails(issueKey).toString());
    }
}