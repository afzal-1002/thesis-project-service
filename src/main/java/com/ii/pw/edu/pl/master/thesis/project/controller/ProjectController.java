package com.ii.pw.edu.pl.master.thesis.project.controller;


import com.ii.pw.edu.pl.master.thesis.project.client.UserClient;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.CreateProjectRequest;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.ProjectResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.ii.pw.edu.pl.master.thesis.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/wut/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final UserClient userClient;
    private final ProjectService projectService;

    @GetMapping("/users")
    public List<UserSummary> getAllUsersViaUserService() {
        return userClient.getAllUsers();
    }

    // ─────────────── CREATE (Jira + local DB via service) ───────────────
    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> create(@RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.createProjectLocalDb(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─────────────── READ ───────────────
//    @GetMapping("/{id}")
//    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
//        ProjectResponse resp = projectService.getProjectById(id); // implement in service if not present
//        return ResponseEntity.ok(resp);
//    }
//
//    @GetMapping("/by-key/{key}")
//    public ResponseEntity<ProjectResponse> getByKey(@PathVariable String key) {
//        ProjectResponse resp = projectService.getProjectByKey(key); // implement in service if not present
//        return ResponseEntity.ok(resp);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ProjectResponse>> list(
//            @RequestParam(value = "baseUrl", required = false) String baseUrl) {
//        List<ProjectResponse> items = (baseUrl == null || baseUrl.isBlank())
//                ? projectService.getAllProjects()                 // implement if not present
//                : projectService.getProjectsByBaseUrl(baseUrl);   // implement if not present
//        return ResponseEntity.ok(items);
//    }
//
//    // ─────────────── UPDATE (optional example) ───────────────
//    @PutMapping("/{id}/name")
//    public ResponseEntity<ProjectResponse> rename(
//            @PathVariable Long id,
//            @RequestParam("newName") String newName) {
//        ProjectResponse resp = projectService.renameProject(id, newName); // implement if needed
//        return ResponseEntity.ok(resp);
//    }
//
//    // ─────────────── DELETE ───────────────
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable Long id) {
//        projectService.deleteProject(id); // implement in service if not present
//    }

}