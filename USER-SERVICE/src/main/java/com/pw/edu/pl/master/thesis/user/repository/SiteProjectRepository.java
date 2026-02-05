package com.pw.edu.pl.master.thesis.user.repository;

import com.pw.edu.pl.master.thesis.user.model.site.SiteProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteProjectRepository extends JpaRepository<SiteProject, Long> {
    List<SiteProject> findBySiteId(Long siteId);
    Optional<SiteProject> findBySiteIdAndKey(Long siteId, String key);
}
