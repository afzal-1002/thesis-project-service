package com.pw.edu.pl.master.thesis.user.controller;

import com.pw.edu.pl.master.thesis.user.dto.site.AddSiteRequest;
import com.pw.edu.pl.master.thesis.user.dto.site.SiteResponse;
import com.pw.edu.pl.master.thesis.user.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wut/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    // ─────────────── CREATE ───────────────
    @PostMapping
    public ResponseEntity<SiteResponse> addSite(@RequestBody AddSiteRequest request) {
        SiteResponse response = siteService.addNewSite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────── READ ───────────────
    @GetMapping("/{siteId}")
    public ResponseEntity<SiteResponse> getSiteById(@PathVariable Long siteId) {
        return ResponseEntity.ok(siteService.findSiteById(siteId));
    }

    @GetMapping("/by-url")
    public ResponseEntity<SiteResponse> getSiteByURL(@RequestParam("baseURL") String baseURL) {
        Optional<SiteResponse> site = siteService.getSiteByURL(baseURL);
        return site.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name")
    public ResponseEntity<SiteResponse> getSiteByName(@RequestParam("siteName") String siteName) {
        SiteResponse site = siteService.findSiteByName(siteName);
        return ResponseEntity.ok(site);
    }


    @GetMapping("/current-user")
    public ResponseEntity<List<SiteResponse>> listMySites() {
        return ResponseEntity.ok(siteService.listMySitesWithProjects());
    }


    // ─────────────── UPDATE ───────────────
    @PutMapping("/{siteId}/name")
    public ResponseEntity<SiteResponse> updateSiteName(
            @PathVariable Long siteId,
            @RequestParam("newSiteName") String newSiteName) {
        return ResponseEntity.ok(siteService.updateSiteName(siteId, newSiteName)); // username ignored
    }

    @PutMapping("/{siteId}/url")
    public ResponseEntity<SiteResponse> updateSiteURL(
            @PathVariable Long siteId,
            @RequestParam("newBaseURL") String newBaseURL) {
        return ResponseEntity.ok(siteService.updateSiteURL(siteId, newBaseURL)); // username ignored
    }

    // ─────────────── DELETE ───────────────
    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> deleteSiteById(@PathVariable Long siteId) {
        siteService.deleteSiteById(siteId); // username ignored
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Void> deleteSiteByName(@RequestParam("siteName") String siteName) {
        siteService.deleteSiteByName(siteName); // username ignored
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-url")
    public ResponseEntity<Void> deleteSiteByURL(@RequestParam("baseURL") String baseURL) {
        siteService.deleteSiteByURL(baseURL); // username ignored
        return ResponseEntity.noContent().build();
    }
}
