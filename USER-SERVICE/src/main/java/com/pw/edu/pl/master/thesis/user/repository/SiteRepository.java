package com.pw.edu.pl.master.thesis.user.repository;

import com.pw.edu.pl.master.thesis.user.model.site.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByBaseUrl(String baseUrl);
    Optional<Site> findBySiteName(String siteName);

    @Query("""
        select s from Site s
        left join fetch s.projects
        where s.id = :id
    """)
    Optional<Site> findByIdWithProjects(@Param("id") Long id);

    @Query("""
       SELECT s
       FROM Site s
       LEFT JOIN FETCH s.projects p
       WHERE LOWER(s.siteName) = LOWER(:name)
       """)
    Optional<Site> findBySiteNameIgnoreCaseWithProjects(@Param("name") String name);



}
