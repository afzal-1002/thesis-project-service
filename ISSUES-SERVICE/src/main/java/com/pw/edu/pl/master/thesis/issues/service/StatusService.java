package com.pw.edu.pl.master.thesis.issues.service;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusResponse;
import com.pw.edu.pl.master.thesis.issues.model.status.Status;

import java.util.List;
import java.util.Optional;

public interface StatusService {

    // 🔄 Synchronize statuses for a specific Jira project
    List<StatusResponse> synchronizeStatusesForProject(String projectKey);

    // 📥 Read from Jira
    List<StatusResponse> getStatusesByProjectKey(String projectKey);
    List<StatusResponse> getAllStatusesFromJira();

    // 💾 Database accessors
    List<Status> getAllStatuses();
    Status getStatusById(String id);
    Optional<Status> getStatusByName(String name);
    List<Status> getStatusesByCategoryName(String categoryName);
    Status createOrUpdateStatus(Status status);
    Status saveStausIfNotExistsOrUpdate(Status status);
}
