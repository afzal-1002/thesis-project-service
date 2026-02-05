package com.pw.edu.pl.master.thesis.user.service;

import com.pw.edu.pl.master.thesis.user.dto.site.AddSiteRequest;
import com.pw.edu.pl.master.thesis.user.dto.site.SiteProjectSummary;
import com.pw.edu.pl.master.thesis.user.dto.site.SiteResponse;
import com.pw.edu.pl.master.thesis.user.model.project.ProjectSummary;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SiteService {

    // Create
    SiteResponse addNewSite(AddSiteRequest request);

    // Update (always uses Basic-auth user)
    SiteResponse updateSiteName(Long siteId, String newSiteName);
    SiteResponse updateSiteURL(Long siteId, String newBaseURL);

    // Delete (always uses Basic-auth user)
    void deleteSiteById(Long siteId);
    void deleteSiteByName(String siteName);
    void deleteSiteByURL(String baseURL);

    // Reads (scoped to Basic-auth user)
    @Transactional(readOnly = true)
    public SiteResponse findSiteById(Long siteId);

    SiteResponse findSiteByName(String siteName);
    Optional<SiteResponse> getSiteByURL(String baseURL);

    public List<SiteProjectSummary> getAllProjectsBySiteName(String siteName);
    List<ProjectSummary> listAllProjects(String siteBaseUrl, String username, String apiToken);
    public List<SiteResponse> listMySitesWithProjects() ;
}
