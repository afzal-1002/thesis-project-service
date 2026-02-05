package com.pw.edu.pl.master.thesis.user.service.implementation;

import com.pw.edu.pl.master.thesis.user.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.user.dto.site.AddSiteRequest;
import com.pw.edu.pl.master.thesis.user.dto.site.SiteProjectSummary;
import com.pw.edu.pl.master.thesis.user.dto.site.SiteResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.JiraUserResponse;
import com.pw.edu.pl.master.thesis.user.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.user.exception.ConflictException;
import com.pw.edu.pl.master.thesis.user.exception.NotFoundException;
import com.pw.edu.pl.master.thesis.user.mapper.SiteMapper;
import com.pw.edu.pl.master.thesis.user.model.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.user.model.project.ProjectSearchPage;
import com.pw.edu.pl.master.thesis.user.model.project.ProjectSummary;
import com.pw.edu.pl.master.thesis.user.model.site.Site;
import com.pw.edu.pl.master.thesis.user.model.site.SiteProject;
import com.pw.edu.pl.master.thesis.user.model.site.SiteURLUtility;
import com.pw.edu.pl.master.thesis.user.model.site.UserSite;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.repository.SiteProjectRepository;
import com.pw.edu.pl.master.thesis.user.repository.SiteRepository;
import com.pw.edu.pl.master.thesis.user.repository.UserRepository;
import com.pw.edu.pl.master.thesis.user.repository.UserSiteRepository;
import com.pw.edu.pl.master.thesis.user.service.CredentialService;
import com.pw.edu.pl.master.thesis.user.service.EncryptionService;
import com.pw.edu.pl.master.thesis.user.service.JiraUserService;
import com.pw.edu.pl.master.thesis.user.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImplementation implements SiteService {

    private final SiteRepository siteRepo;
    private final SiteProjectRepository siteProjectRepo;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final UserRepository userRepo;
    private final UserSiteRepository userSiteRepo;
    private final JiraUserService jiraUserService;
    private final SiteMapper mapper;
    private final CredentialService credentialService;
    private final EncryptionService encryptionService;
    private final JiraClientConfiguration jiraClientConfiguration;

    // ─────────────────────────────────────────────────────────────────────
    // Create
    // ─────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public SiteResponse addNewSite(AddSiteRequest request) {
        if (request == null) throw new IllegalArgumentException("request is null");
        if (StringUtils.isBlank(request.getSiteName()))
            throw new IllegalArgumentException("siteName is required");

        final String siteName = request.getSiteName().trim();

        // ── Resolve baseURL either from hostPart or baseUrl, normalize, and validate ──
        String hostPart = trimToNull(request.getHostPart());
        String baseURL  = trimToNull(request.getBaseUrl());

        if (hostPart != null && baseURL != null) {
            String builtFromHost      = SiteURLUtility.buildURLFromHostPart(hostPart);
            String normalizedProvided = SiteURLUtility.normalizeURL(baseURL);
            if (!normalizedProvided.equalsIgnoreCase(builtFromHost)) {
                throw new IllegalArgumentException(
                        "Provided baseURL does not match hostPart-derived URL. " +
                                "hostPart URL = " + builtFromHost + ", provided baseURL = " + normalizedProvided
                );
            }
            baseURL = normalizedProvided;
        } else if (hostPart != null) {
            baseURL = SiteURLUtility.buildURLFromHostPart(hostPart);
        } else if (baseURL != null) {
            baseURL = SiteURLUtility.normalizeURL(baseURL);
        } else {
            throw new IllegalArgumentException("Provide either hostPart or baseUrl");
        }

        // Always normalize to full https://<tenant>.atlassian.net (and cache in session)
        baseURL = jiraUrlBuilder.normalizeJiraBaseUrl(baseURL);

        // Enforce uniqueness (one Site per baseURL)
        final String finalBaseURL = baseURL;
        siteRepo.findByBaseUrl(finalBaseURL).ifPresent(s -> {
            throw new ConflictException("A site with this baseURL already exists: " + finalBaseURL);
        });

        // ── Resolve acting credentials coming from the request (plain token) ──
        final String actingUsername = trimToNull(request.getUsername());
        final String tokenPlain     = trimToNull(request.getJiraToken()); // PLAIN token (no decrypt)

        if (StringUtils.isBlank(actingUsername) || StringUtils.isBlank(tokenPlain)) {
            throw new IllegalArgumentException("username and jiraToken are required in the request.");
        }

        // ── Verify Jira /myself for this tenant with provided credentials ──
        try {
            JiraUserResponse me = jiraUserService.getJiraUserDetailsRaw(finalBaseURL, actingUsername, tokenPlain);
            if (me == null || StringUtils.isBlank(me.getAccountId())) {
                throw new IllegalStateException("Jira /myself did not return a valid accountId");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unable to verify Jira site with provided credentials at baseURL: " + finalBaseURL +
                            " — cause: " + e.getMessage(), e
            );
        }

        // ── Persist Site ─────────────────────────────────────────────────────
        Site site = Site.builder()
                .siteName(siteName)
                .hostPart(hostPart)
                .baseUrl(finalBaseURL)
                .build();
        site = siteRepo.save(site);

        // ── Link App User ↔ Site in user_site (defaultForUser = true) ───────
        User domainUser = userRepo.findByUsername(actingUsername)
                .orElseThrow(() -> new IllegalArgumentException("App user not found: " + actingUsername));

        if (!userSiteRepo.existsByUserIdAndSiteId(domainUser.getId(), site.getId())) {
            userSiteRepo.save(
                    UserSite.builder()
                            .user(domainUser)
                            .site(site)
                            .defaultForUser(Boolean.TRUE)
                            .build()
            );
        }

        // ── Fetch ALL Jira projects for this tenant and save into site_project ─
        // Uses typed DTOs (ProjectSummary) and your pagination
        List<ProjectSummary> allProjects = listAllProjects(finalBaseURL, actingUsername, tokenPlain);

        // Avoid duplicates per (site_id, project_key)
        var existing = siteProjectRepo.findBySiteId(site.getId());
        var existingKeys = existing.stream()
                .map(SiteProject::getKey)
                .collect(java.util.stream.Collectors.toSet());

        List<SiteProject> toInsert = new ArrayList<>();
        for (ProjectSummary p : allProjects) {
            String key     = trimToNull(p.getKey());
            String name    = trimToNull(p.getName());
            String jiraId  = trimToNull(p.getId());

            if (StringUtils.isBlank(key) || StringUtils.isBlank(name)) continue;
            if (existingKeys.contains(key)) continue; // already present for this site

            toInsert.add(
                    SiteProject.builder()
                            .site(site)
                            .jiraId(jiraId)
                            .key(key)
                            .name(name)
                            .build()
            );
        }

        if (!toInsert.isEmpty()) {
            siteProjectRepo.saveAll(toInsert); // projects saved into site_project
        }

        return mapper.toResponse(site);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Update (use current principal; enforce ownership)
    // ─────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public SiteResponse updateSiteName(Long siteId, String newSiteName) {
        if (siteId == null) throw new IllegalArgumentException("siteId is required");
        if (StringUtils.isBlank(newSiteName)) throw new IllegalArgumentException("newSiteName is required");

        // Load with projects so the response includes them
        Site site = siteRepo.findByIdWithProjects(siteId)
                .orElseThrow(() -> new NotFoundException("Site not found"));

        // Must be owned/linked by the current authenticated user (Basic auth)
        assertLinkedToCurrentUserOrThrow(site.getId());

        String trimmed = newSiteName.trim();

        // Ensure unique siteName (excluding this site)
        siteRepo.findBySiteName(trimmed)
                .filter(other -> !other.getId().equals(siteId))
                .ifPresent(other -> { throw new ConflictException("siteName already in use: " + trimmed); });

        site.setSiteName(trimmed);
        siteRepo.save(site);

        // Re-load to ensure projects are present and mapped
        Site persisted = siteRepo.findByIdWithProjects(siteId)
                .orElseThrow(() -> new NotFoundException("Site not found"));

        return mapToSiteResponse(persisted);
    }


    @Override
    @Transactional
    public SiteResponse updateSiteURL(Long siteId, String newBaseURL) {
        if (siteId == null) throw new IllegalArgumentException("siteId is required");
        if (StringUtils.isBlank(newBaseURL)) throw new IllegalArgumentException("newBaseURL is required");

        // 1) Load the site and enforce ownership (current Basic-auth principal)
        Site site = siteRepo.findByIdWithProjects(siteId)
                .orElseThrow(() -> new NotFoundException("Site not found"));
        assertLinkedToCurrentUserOrThrow(site.getId());

        // 2) Normalize and ensure uniqueness (excluding this site)
        String normalized = jiraUrlBuilder.normalizeJiraBaseUrl(
                SiteURLUtility.normalizeURL(newBaseURL));
        siteRepo.findByBaseUrl(normalized)
                .filter(other -> !other.getId().equals(siteId))
                .ifPresent(other -> { throw new ConflictException("baseURL already in use: " + normalized); });

        // 3) Validate the new base URL against Jira /myself using the CURRENT user's credential
        UserCredential cred = currentCredential(); // Basic-auth derived
        String jiraEmail  = cred.getUsername();
        String tokenPlain = encryptionService.decrypt(cred.getToken()).getResult();
        try {
            JiraUserResponse me = jiraUserService.getJiraUserDetailsRaw(normalized, jiraEmail, tokenPlain);
            if (me == null || StringUtils.isBlank(me.getAccountId())) {
                throw new IllegalStateException("Jira /myself did not return a valid accountId");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to verify Jira at baseURL: " + normalized + " — cause: " + e.getMessage(), e);
        }

        // 4) Persist the new URL
        site.setBaseUrl(normalized);
        siteRepo.save(site);

        // 5) Refresh Site Projects from Jira (upsert by project key; remove stale)
        //    If you’ve already switched to a typed ProjectSummary, replace the listAllProjects(...)
        //    with your typed variant and adapt the mapping below.
        List<ProjectSummary> remote = listAllProjects(normalized, jiraEmail, tokenPlain);

        // Build "existing by key" map for quick upsert
        List<SiteProject> existing = siteProjectRepo.findBySiteId(site.getId());
        Map<String, SiteProject> byKey = existing.stream()
                .collect(java.util.stream.Collectors.toMap(SiteProject::getKey, sp -> sp));

        // Track keys we saw remotely to detect deletions
        java.util.Set<String> seenKeys = new java.util.HashSet<>();

        // Upsert new/changed
        for (ProjectSummary p : remote) {
            String key   = p.getKey()  != null ? String.valueOf(p.getKey())  : null;
            String name  = p.getName() != null ? String.valueOf(p.getName()) : null;
            String jiraId= p.getId()   != null ? String.valueOf(p.getId())   : null;
            if (StringUtils.isBlank(key) || StringUtils.isBlank(name)) continue;

            seenKeys.add(key);

            SiteProject existingSp = byKey.get(key);
            if (existingSp == null) {
                // insert
                SiteProject sp = SiteProject.builder()
                        .site(site)
                        .jiraId(jiraId)
                        .key(key)
                        .name(name)
                        .build();
                siteProjectRepo.save(sp);
            } else {
                // update if name/id changed
                boolean dirty = false;
                if (!StringUtils.equals(existingSp.getName(), name)) { existingSp.setName(name); dirty = true; }
                if (!StringUtils.equals(existingSp.getJiraId(), jiraId)) { existingSp.setJiraId(jiraId); dirty = true; }
                if (dirty) siteProjectRepo.save(existingSp);
            }
        }

        // Remove projects that no longer exist remotely (optional but recommended)
        for (SiteProject sp : existing) {
            if (!seenKeys.contains(sp.getKey())) {
                siteProjectRepo.delete(sp);
            }
        }

        // 6) Re-load with projects and return a complete DTO
        Site fresh = siteRepo.findByIdWithProjects(siteId)
                .orElseThrow(() -> new NotFoundException("Site not found"));
        return mapper.toResponse(fresh);
    }


    // ─────────────────────────────────────────────────────────────────────
    // Deletes (use current principal; enforce ownership)
    // ─────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteSiteById(Long siteId) {
        Site site = getSiteForCurrentUserOrThrow(siteId);
        userSiteRepo.deleteAll(userSiteRepo.findBySiteId(site.getId()));
        siteRepo.delete(site);
    }

    @Override
    @Transactional
    public void deleteSiteByName(String siteName) {
        if (StringUtils.isBlank(siteName)) throw new IllegalArgumentException("siteName is required");
        Site site = siteRepo.findBySiteName(siteName.trim())
                .orElseThrow(() -> new NotFoundException("Site not found"));
        assertLinkedToCurrentUserOrThrow(site.getId());
        deleteSiteById(site.getId());
    }

    @Override
    @Transactional
    public void deleteSiteByURL(String baseURL) {
        if (StringUtils.isBlank(baseURL)) throw new IllegalArgumentException("baseURL is required");
        String normalized = jiraUrlBuilder.normalizeJiraBaseUrl(SiteURLUtility.normalizeURL(baseURL));
        Site site = siteRepo.findByBaseUrl(normalized)
                .orElseThrow(() -> new NotFoundException("Site not found"));
        assertLinkedToCurrentUserOrThrow(site.getId());
        deleteSiteById(site.getId());
    }



    @Override
    @Transactional(readOnly = true)
    public SiteResponse findSiteByName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("siteName is required");
        }

        // 1) Current user from Basic Auth
        User currentUser = currentDomainUser();

        // 2) Normalize input (trim; repo will handle case-insensitive match)
        String probe = siteName.trim();

        // 3) Load site + projects with case-insensitive name
        Site site = siteRepo.findBySiteNameIgnoreCaseWithProjects(probe)
                .orElseThrow(() -> new NotFoundException("Site not found"));

        // 4) Enforce ownership
        boolean linked = userSiteRepo.existsByUserIdAndSiteId(currentUser.getId(), site.getId());
        if (!linked) {
            // avoid leaking existence
            throw new NotFoundException("Site not found");
        }

        // 5) Map to DTO (includes project summaries)
        return mapToSiteResponse(site);
    }


    @Override
    public Optional<SiteResponse> getSiteByURL(String baseURL) {
        if (StringUtils.isBlank(baseURL)) return Optional.empty();
        String normalized = jiraUrlBuilder.normalizeJiraBaseUrl(SiteURLUtility.normalizeURL(baseURL));
        Optional<Site> siteOpt = siteRepo.findByBaseUrl(normalized);
        if (siteOpt.isEmpty()) return Optional.empty();
        Site site = siteOpt.get();
        if (!isLinkedToCurrentUser(site.getId())) return Optional.empty();
        return Optional.of(mapper.toResponse(site));
    }



    // ─────────────────────────────────────────────────────────────────────
    // Aggregations (scoped to ownership where applicable)
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public List<SiteProjectSummary> getAllProjectsBySiteName(String siteName) {
        if (StringUtils.isBlank(siteName)) throw new IllegalArgumentException("siteName is required");
        Site site = siteRepo.findBySiteName(siteName.trim())
                .orElseThrow(() -> new NotFoundException("Site not found: " + siteName));
        assertLinkedToCurrentUserOrThrow(site.getId());
        List<SiteProject> projects = siteProjectRepo.findBySiteId(site.getId());
        return projects.stream().map(mapper::toProjectSummary).toList();
    }



    @Override
    public List<ProjectSummary> listAllProjects(
            String siteBaseUrl,
            String providedUsername,
            String providedApiToken) {

        String jiraEmail;
        String tokenPlain;
        if (StringUtils.isNotBlank(providedUsername) && StringUtils.isNotBlank(providedApiToken)) {
            jiraEmail  = providedUsername.trim();
            tokenPlain = providedApiToken;
        } else {
            UserCredential cred = currentCredential();
            jiraEmail  = cred.getUsername();
            tokenPlain = encryptionService.decrypt(cred.getToken()).getResult();
        }

        String base = jiraUrlBuilder.normalizeJiraBaseUrl(siteBaseUrl);

        int startAt = 0;
        int pageSize = 100;
        List<ProjectSummary> all = new ArrayList<>();

        while (true) {
            String baseEndpoint = jiraUrlBuilder.url(base, JiraApiEndpoint.PROJECT_SEARCH);
            String url = baseEndpoint + "?startAt=" + startAt + "&maxResults=" + pageSize;

            ProjectSearchPage page = jiraClientConfiguration.get(
                    url, ProjectSearchPage.class, jiraEmail, tokenPlain);

            if (page.getValues() != null && !page.getValues().isEmpty()) {
                all.addAll(page.getValues());
            }

            int fetched = (page.getValues() == null) ? 0 : page.getValues().size();
            int total   = (page.getTotal() == null) ? all.size() : page.getTotal();

            if (all.size() >= total || fetched == 0) break;
            startAt += pageSize;
        }

        return all;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers (current principal, ownership checks, normalize)
    // ─────────────────────────────────────────────────────────────────────
    private UserCredential currentCredential() {
        return credentialService.getForCurrentUserOrThrow();
    }

    private User currentDomainUser() {
        String username = getAuthenticatedUsernameOrThrow();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    private Site getSiteForCurrentUserOrThrow(Long siteId) {
        if (siteId == null) throw new IllegalArgumentException("siteId is required");
        Site site = siteRepo.findByIdWithProjects(siteId)
                .orElseThrow(() -> new NotFoundException("Site not found"));
        assertLinkedToCurrentUserOrThrow(site.getId());
        return site;
    }

    private void assertLinkedToCurrentUserOrThrow(Long siteId) {
        User user = currentDomainUser();
        boolean linked = userSiteRepo.existsByUserIdAndSiteId(user.getId(), siteId);
        if (!linked) {
            // Hide existence vs. authorization; return 404 to avoid info leaks
            throw new NotFoundException("Site not found");
        }
    }

    // TRUE when the current user IS linked
    private boolean isLinkedToCurrentUser(Long siteId) {
        User user = currentDomainUser();
        return userSiteRepo.existsByUserIdAndSiteId(user.getId(), siteId);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }


    /** Strictly require an authenticated principal. */
    private String getAuthenticatedUsernameOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
                auth.getPrincipal() == null ||
                "anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()))) {
            throw new IllegalStateException("Unauthenticated request (no Basic auth principal).");
        }
        return auth.getName();
    }

    @Override
    @Transactional(readOnly = true)
    public SiteResponse findSiteById(Long siteId) {
        if (siteId == null) throw new IllegalArgumentException("siteId is required");

        // 1) Resolve current user from Basic Auth principal
        User currentUser = currentDomainUser(); // uses SecurityContext -> username -> users repo

        // 2) Load site + projects
        Site site = siteRepo.findByIdWithProjects(siteId)
                .orElseThrow(() -> new NotFoundException("Site not found"));

        // 3) Enforce ownership (only return if caller is linked to this site)
        boolean linked = userSiteRepo.existsByUserIdAndSiteId(currentUser.getId(), site.getId());
        if (!linked) {
            // Hide existence vs authorization
            throw new NotFoundException("Site not found");
        }

        // 4) Build response including projects
        return mapToSiteResponse(site);
    }


/* =========================
   Small, explicit mappers
   ========================= */

    private SiteResponse mapToSiteResponse(Site site) {
        List<SiteProjectSummary> projectDtos = site.getProjects() == null
                ? List.of()
                : site.getProjects().stream()
                .map(this::mapToProjectSummary)
                .toList();

        return SiteResponse.builder()
                .id(site.getId())
                .siteName(site.getSiteName())
                .hostPart(site.getHostPart())
                .baseURL(site.getBaseUrl())                 // entity uses baseUrl; DTO uses baseURL
                .createdAt(site.getCreationDate())          // entity: creationDate
                .updatedAt(site.getUpdateDate())            // entity: updateDate
                .projects(projectDtos)
                .build();
    }

    private SiteProjectSummary mapToProjectSummary(SiteProject p) {
        // Adapt the fields to **your** SiteProjectSummary DTO
        // (common shape shown; rename if your DTO differs)
        return SiteProjectSummary.builder()
                .id(p.getId())
                .projectKey(p.getKey())
                .projectName(p.getName())
                .jiraId(p.getJiraId())
                .build();
    }

    private record AuthCredentials(String username, String tokenPlain) {}

    private AuthCredentials resolveAuthCredentialsForAdd(AddSiteRequest request) {
        // If there is an authenticated principal, use stored credentials
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()));

        if (authenticated) {
            UserCredential cred = credentialService.getForCurrentUserOrThrow();
            String user = cred.getUsername();
            String tokenPlain = encryptionService.decrypt(cred.getToken()).getResult();
            return new AuthCredentials(user, tokenPlain);
        }

        // Otherwise, we expect the request to carry the Jira login and PLAIN token
        // NOTE: Your AddSiteRequest must have these fields if you want /register to call addNewSite
        // e.g., private String username; private String jiraToken; (jiraToken should be PLAIN here)
        if (request == null || StringUtils.isBlank(request.getUsername()))
            throw new IllegalArgumentException("username is required in request when not authenticated");
        if (request.getJiraToken() == null || request.getJiraToken().isBlank())
            throw new IllegalArgumentException("jiraToken (plain) is required in request when not authenticated");

        return new AuthCredentials(request.getUsername().trim(), request.getJiraToken());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteResponse> listMySitesWithProjects() {
        // 1) Who is calling? (Basic Auth)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()))) {
            throw new IllegalStateException("Unauthenticated request (no Basic auth principal).");
        }
        String username = auth.getName();

        // 2) Domain user
        User domainUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        // 3) All sites for this user (via user_site)
        var links = userSiteRepo.findByUserId(domainUser.getId());
        if (links.isEmpty()) return List.of();

        // 4) For each site, ensure projects are included, then map to DTO
        return links.stream()
                .map(UserSite::getSite)
                .distinct()
                .map(site -> {
                    // If Site.projects is LAZY, fetch explicitly to be safe
                    List<SiteProject> projects = siteProjectRepo.findBySiteId(site.getId());

                    // Build the DTO including projects
                    List<SiteProjectSummary> projectDtos = projects.stream()
                            .map(p -> SiteProjectSummary.builder()
                                    .id(p.getId())
                                    .projectKey(p.getKey())
                                    .projectName(p.getName())
                                    .jiraId(p.getJiraId())
                                    .build())
                            .toList();

                    return SiteResponse.builder()
                            .id(site.getId())
                            .siteName(site.getSiteName())
                            .hostPart(site.getHostPart())
                            .baseURL(site.getBaseUrl())
                            .createdAt(site.getCreationDate())
                            .updatedAt(site.getUpdateDate())
                            .projects(projectDtos)
                            .build();
                })
                .toList();
    }




}
