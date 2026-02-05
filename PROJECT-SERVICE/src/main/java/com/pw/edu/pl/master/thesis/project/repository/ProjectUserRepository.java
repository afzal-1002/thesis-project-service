package com.pw.edu.pl.master.thesis.project.repository;

import com.pw.edu.pl.master.thesis.project.model.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {
    List<ProjectUser> findByProject_Id(Long projectId);
    long countByProject_Id(Long projectId);
    boolean existsByProject_IdAndUserId(Long projectId, String userId);
    void deleteByProject_IdAndUserId(Long projectId, String userId);
}

