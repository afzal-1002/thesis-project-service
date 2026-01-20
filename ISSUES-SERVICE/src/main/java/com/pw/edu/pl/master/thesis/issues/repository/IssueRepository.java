package com.pw.edu.pl.master.thesis.issues.repository;

import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    boolean existsByKey(String key);

    Optional<Issue> findByKey(String key);
    Optional<Issue> findByJiraId(String issueId);

    // Project is a String field `projectKey`, not a relation
    List<Issue> findByProjectKey(String projectKey);
    List<Issue> findAllByProjectKey(String projectKey);

    // Assignee is a String field `assigneeId`, not an embedded account
    List<Issue> findByAssigneeId(String assigneeId);
    List<Issue> findAllByAssigneeId(String assigneeId);

    // OPTIONAL convenience deletes (safe & type-correct); use as needed
    long deleteByProjectKey(String projectKey);
    long deleteByAssigneeId(String assigneeId);
    void deleteByKeyIn(Collection<String> keys);

    @Query("select i from Issue i where i.key = :key")
    Optional<Issue> findManagedByKey(@Param("key") String key);


}
