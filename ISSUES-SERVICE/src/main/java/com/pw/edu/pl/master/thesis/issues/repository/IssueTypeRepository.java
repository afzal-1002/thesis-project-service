package com.pw.edu.pl.master.thesis.issues.repository;

import com.pw.edu.pl.master.thesis.issues.model.issuetype.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueTypeRepository extends JpaRepository<IssueType, Long> {

    Optional<IssueType> findByName(String name);
    Optional<IssueType> findByDescription(String description);
    Optional<IssueType> findByIconUrl(String iconUrl);
    Optional<IssueType> findBySubtask(Boolean subtask);

    Optional<IssueType> findByJiraId(String jiraId);

    Optional<IssueType> findByNameIgnoreCase(String name);

    // (Optional helpers)
    boolean existsByJiraId(String jiraId);
    boolean existsByNameIgnoreCase(String name);
}
