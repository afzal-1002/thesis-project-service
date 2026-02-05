package com.pw.edu.pl.master.thesis.project.service.implementation;


import com.pw.edu.pl.master.thesis.project.client.CredentialClient;
import com.pw.edu.pl.master.thesis.project.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.project.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.project.model.Project;
import com.pw.edu.pl.master.thesis.project.model.ProjectUser;
import com.pw.edu.pl.master.thesis.project.repository.ProjectRepository;
import com.pw.edu.pl.master.thesis.project.repository.ProjectUserRepository;
import com.pw.edu.pl.master.thesis.project.service.ProjectUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectUserServiceImplementation implements ProjectUserService {

    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;
    private final CredentialClient credentialClient;
    private final JiraUrlBuilder jiraUrlBuilder;

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ProjectUser> listMembers(String projectKeyOrId) {
        Project project = requireProjectForCaller(projectKeyOrId);
        return projectUserRepository.findByProject_Id(project.getId());
    }

    @Override
    @Transactional
    public boolean addMember(String projectKeyOrId, String userId, String displayName) {
        if (isBlank(userId)) throw new IllegalArgumentException("userId is required");

        Project project = requireProjectForCaller(projectKeyOrId);

        if (projectUserRepository.existsByProject_IdAndUserId(project.getId(), userId)) {
            return false; // already linked
        }

        ProjectUser link = new ProjectUser();
        link.setProject(project);
        link.setUserId(userId.trim());
        link.setUsername(isBlank(displayName) ? userId.trim() : displayName.trim()); // ensure NOT NULL username

        projectUserRepository.save(link);
        return true;
    }

    @Override
    @Transactional
    public boolean removeMember(String projectKeyOrId, String userId) {
        if (isBlank(userId)) throw new IllegalArgumentException("userId is required");

        Project project = requireProjectForCaller(projectKeyOrId);

        if (!projectUserRepository.existsByProject_IdAndUserId(project.getId(), userId)) {
            return false; // nothing to delete
        }
        projectUserRepository.deleteByProject_IdAndUserId(project.getId(), userId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long countMembers(String projectKeyOrId) {
        Project project = requireProjectForCaller(projectKeyOrId);
        return projectUserRepository.countByProject_Id(project.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String projectKeyOrId, String userId) {
        if (isBlank(userId)) throw new IllegalArgumentException("userId is required");
        Project project = requireProjectForCaller(projectKeyOrId);
        return projectUserRepository.existsByProject_IdAndUserId(project.getId(), userId);
    }

    // ─────────────────────────────────────────────
    // Helpers — scope to caller via Basic Auth (/credentials/me)
    // ─────────────────────────────────────────────
    private Project requireProjectForCaller(String keyOrId) {
        String k = requireNotBlank(keyOrId, "projectKeyOrId is required").trim();
        UserCredentialResponse cred = currentCred(); // from Authorization header relayed to user-service
        String baseUrl = cred.getBaseUrl();

        // Prefer KEY match, fallback to Jira ID
        Project project = projectRepository.findByKeyAndBaseUrl(k.toUpperCase(), baseUrl)
                .orElseGet(() -> projectRepository.findByJiraIdAndBaseUrl(k, baseUrl).orElse(null));

        if (project == null) {
            throw new IllegalArgumentException("Project not found for keyOrId='" + k + "' at baseUrl='" + baseUrl + "'");
        }
        return project;
    }

    private UserCredentialResponse currentCred() {
        UserCredentialResponse cred = credentialClient.getMine(); // Feign must relay Basic header
        if (cred == null || isBlank(cred.getBaseUrl())) {
            throw new IllegalStateException("Missing Jira baseUrl for current user");
        }
        cred.setBaseUrl(jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl()));
        if (isBlank(cred.getUsername())) {
            throw new IllegalStateException("Missing Jira username for current user");
        }
        return cred;
    }

    private static String requireNotBlank(String s, String msg) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(msg);
        return s;
    }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
