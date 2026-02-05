package com.pw.edu.pl.master.thesis.issues.client;


import com.pw.edu.pl.master.thesis.issues.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.issues.dto.site.AddSiteRequest;
import com.pw.edu.pl.master.thesis.issues.dto.site.SiteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(name = "user-service",
//        contextId = "SiteClient",
//        url = "${user.service.url}",
//        path = "/api/wut/sites",
//        configuration = FeignSecurityConfiguration.class)

@FeignClient(
        name = "USER-SERVICE",
        contextId = "SiteClient",
        path = "/api/wut/sites",
        configuration = FeignSecurityConfiguration.class
)
public interface SiteClient {

    @PostMapping
    SiteResponse addSite(@RequestBody AddSiteRequest request);

    @GetMapping("/{siteId}")
    SiteResponse getSiteById(@PathVariable("siteId") Long siteId);

    @GetMapping("/by-url")
    SiteResponse getSiteByURL(@RequestParam("baseURL") String baseURL);

    @GetMapping("/by-name")
    SiteResponse getSiteByName(@RequestParam("siteName") String siteName);

    @GetMapping("/current-user")
    List<SiteResponse> listMySites();

    @PutMapping("/{siteId}/name")
    SiteResponse updateSiteName(@PathVariable("siteId") Long siteId,
                                @RequestParam("newSiteName") String newSiteName);

    @PutMapping("/{siteId}/url")
    SiteResponse updateSiteURL(@PathVariable("siteId") Long siteId,
                               @RequestParam("newBaseURL") String newBaseURL);

    @DeleteMapping("/{siteId}")
    void deleteSiteById(@PathVariable("siteId") Long siteId);

    @DeleteMapping("/by-name")
    void deleteSiteByName(@RequestParam("siteName") String siteName);

    @DeleteMapping("/by-url")
    void deleteSiteByURL(@RequestParam("baseURL") String baseURL);
}
