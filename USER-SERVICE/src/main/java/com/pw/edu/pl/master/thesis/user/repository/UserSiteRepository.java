package com.pw.edu.pl.master.thesis.user.repository;

import com.pw.edu.pl.master.thesis.user.model.site.UserSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSiteRepository extends JpaRepository<UserSite, Long> {
    List<UserSite> findByUserId(Long userId);
    List<UserSite> findBySiteId(Long siteId);
    boolean existsByUserIdAndSiteId(Long userId, Long siteId);
}
