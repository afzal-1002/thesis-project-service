package com.ii.pw.edu.pl.master.thesis.project.repository;

import com.ii.pw.edu.pl.master.thesis.project.model.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByKey(String key);
    Optional<Project> findByJiraIdAndBaseUrl(String jiraId, String baseUrl);

    @EntityGraph(attributePaths = "users")
    Optional<Project> findByKeyAndBaseUrl(String key, String baseUrl);

    @EntityGraph(attributePaths = "users")
    List<Project> findAllByBaseUrl(String baseUrl);


}
