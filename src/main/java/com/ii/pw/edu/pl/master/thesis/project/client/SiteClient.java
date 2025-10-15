package com.ii.pw.edu.pl.master.thesis.project.client;


import com.ii.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.ii.pw.edu.pl.master.thesis.project.dto.site.AddSiteRequest;
import com.ii.pw.edu.pl.master.thesis.project.dto.site.SiteProjectSummary;
import com.ii.pw.edu.pl.master.thesis.project.dto.site.SiteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient( name = "user-service",  contextId = "siteClient",
        url = "${user.service.base-url}",  path = "/api/wut/sites"
)
public interface SiteClient {

    // CREATE
    @PostMapping
    SiteResponse addSite(@RequestBody AddSiteRequest request,
                         @RequestHeader("username") String username,
                         @RequestHeader(value = "token", required = false) String token);

    // READ
    @GetMapping("/{siteId}")
    SiteResponse getSiteById(@PathVariable Long siteId);

    @GetMapping("/by-url")
    Optional<SiteResponse> getSiteByURL(@RequestParam("baseURL") String baseURL,
                                        @RequestHeader("username") String username);

    @GetMapping("/by-name")
    Optional<SiteResponse> getSiteByName(@RequestParam("siteName") String siteName,
                                         @RequestHeader("username") String username);

    @GetMapping
    List<SiteResponse> listSitesForCurrentUser(@RequestHeader("username") String username);

    @GetMapping("/by-username/{username}")
    List<SiteResponse> listSitesForUser(@PathVariable("username") String username);

    // AGGREGATIONS
    @GetMapping("/projects/by-site-name")
    List<SiteProjectSummary> getAllProjectsBySiteName(@RequestParam("siteName") String siteName);

    @GetMapping("/{siteId}/users")
    List<UserSummary> getAllUsersForSiteId(@PathVariable Long siteId);

    @GetMapping("/users/by-site-name")
    List<UserSummary> getAllUsersForSiteName(@RequestParam("siteName") String siteName);

    // UPDATE
    @PutMapping("/{siteId}/name")
    SiteResponse updateSiteName(@PathVariable Long siteId,
                                @RequestParam("newSiteName") String newSiteName,
                                @RequestHeader("username") String username);

    @PutMapping("/{siteId}/url")
    SiteResponse updateSiteURL(@PathVariable Long siteId,
                               @RequestParam("newBaseURL") String newBaseURL,
                               @RequestHeader("username") String username);

    // DELETE
    @DeleteMapping("/{siteId}")
    void deleteSiteById(@PathVariable Long siteId, @RequestHeader("username") String username);

    @DeleteMapping("/by-name")
    void deleteSiteByName(@RequestParam("siteName") String siteName, @RequestHeader("username") String username);

    @DeleteMapping("/by-url")
    void deleteSiteByURL(@RequestParam("baseURL") String baseURL, @RequestHeader("username") String username);
}
