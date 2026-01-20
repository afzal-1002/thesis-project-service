package com.pw.edu.pl.master.thesis.issues.controller;


import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusCategory;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusResponse;
import com.pw.edu.pl.master.thesis.issues.model.status.Status;
import com.pw.edu.pl.master.thesis.issues.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/wut/issue/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    // 🔄 Sync all statuses by Jira project key
    @PostMapping("/sync/by-key/{projectKey}")
    public ResponseEntity<List<StatusResponse>> syncStatusesByProjectKey(@PathVariable String projectKey) {
        return ResponseEntity.ok(statusService.synchronizeStatusesForProject(projectKey));
    }

    // 🔄 Sync all global statuses (from /rest/api/3/statusResponse)
    @PostMapping("/sync/global")
    public ResponseEntity<List<StatusResponse>> syncAllGlobalStatuses() {
        List<StatusResponse> globalStatuses = statusService.getAllStatusesFromJira();
        for (StatusResponse status : globalStatuses) {
            Status mapped = Status.builder()
                    .name(status.getName())
                    .description(status.getDescription())
                    .iconUrl(status.getIconUrl())
                    .category(status.getStatusCategory() != null ? StatusCategory.builder()
                            .id(status.getStatusCategory().getId())
                            .key(status.getStatusCategory().getKey())
                            .name(status.getStatusCategory().getName())
                            .colorName(status.getStatusCategory().getColorName())
                            .build() : null)
                    .build();

            statusService.saveStausIfNotExistsOrUpdate(mapped);
        }
        return ResponseEntity.ok(globalStatuses);
    }

    // 🌐 Get all statuses globally from Jira (without saving)
    @GetMapping("/all")
    public ResponseEntity<List<StatusResponse>> getAllStatusesFromJira() {
        return ResponseEntity.ok(statusService.getAllStatusesFromJira());
    }

    // 🌐 Get all statuses for a specific Jira project key (from Jira API)
    @GetMapping("/project-key/{projectKey}")
    public ResponseEntity<List<StatusResponse>> getStatusesByProjectKey(@PathVariable String projectKey) {
        return ResponseEntity.ok(statusService.getStatusesByProjectKey(projectKey));
    }

    // 📄 Get all saved statuses from local DB
    @GetMapping
    public ResponseEntity<List<Status>> getAllStatuses() {
        return ResponseEntity.ok(statusService.getAllStatuses());
    }

    // 🔍 Get a specific statusResponse by ID from local DB
    @GetMapping("/{statusId}")
    public ResponseEntity<Status> getStatusById(@PathVariable String statusId) {
        return ResponseEntity.ok(statusService.getStatusById(statusId));
    }

    // 📥 Save or update a statusResponse manually
    @PostMapping
    public ResponseEntity<Status> createOrUpdateStatus(@RequestBody Status status) {
        Status saved = statusService.createOrUpdateStatus(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 🔍 Find a statusResponse by name (if available locally)
    @GetMapping("/search/by-name")
    public ResponseEntity<Status> getStatusByName(@RequestParam String name) {
        return statusService.getStatusByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔍 Get statuses filtered by category name (locally)
    @GetMapping("/search/by-category")
    public ResponseEntity<List<Status>> getStatusesByCategory(@RequestParam String category) {
        return ResponseEntity.ok(statusService.getStatusesByCategoryName(category));
    }
}
