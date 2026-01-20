package com.pw.edu.pl.master.thesis.issues.repository;

import com.pw.edu.pl.master.thesis.issues.model.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> { // <<-- CHANGED TO String for ID
    Optional<Status> findByName(String name);
    List<Status> findByCategory_Name(String name);

    // handy extras you likely use elsewhere
    Optional<Status> findByJiraId(String jiraId);

    // if you also query by category key/color/id:
    List<Status> findByCategory_Key(String key);
    List<Status> findByCategory_Id(Long id);
}