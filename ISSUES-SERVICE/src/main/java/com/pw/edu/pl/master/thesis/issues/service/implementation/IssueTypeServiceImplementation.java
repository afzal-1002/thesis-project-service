package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.pw.edu.pl.master.thesis.issues.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.issues.configuration.RequestCredentials;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.CreateIssueTypeRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.CreateIssueTypeResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeReference;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeSummary;
import com.pw.edu.pl.master.thesis.issues.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.issues.exception.CustomException;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.mapper.IssueTypeMapper;
import com.pw.edu.pl.master.thesis.issues.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.issues.model.issuetype.IssueType;
import com.pw.edu.pl.master.thesis.issues.repository.IssueTypeRepository;
import com.pw.edu.pl.master.thesis.issues.service.IssueTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueTypeServiceImplementation implements IssueTypeService {

    private final IssueTypeRepository issueTypeRepository;
    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final IssueTypeMapper issueTypeMapper;
    private final RequestCredentials credentials;


    @Override
    @Transactional(readOnly = true)
    public IssueType getById(String jiraId) {
        if (jiraId == null || jiraId.isBlank()) {
            throw new IllegalArgumentException("jiraId must not be null/blank");
        }
        return issueTypeRepository.findByJiraId(jiraId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "IssueType not found for jiraId=" + jiraId));
    }

    @Override
    @Transactional(readOnly = true)
    public IssueType getByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null/blank");
        }
        return issueTypeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "IssueType not found for name=" + name));
    }

    @Override
    @Transactional
    public IssueType findOrCreateBIdOrName(String jiraId, String name) {
        if (jiraId != null && !jiraId.isBlank()) {
            var byJira = issueTypeRepository.findByJiraId(jiraId);
            if (byJira.isPresent()) {
                var it = byJira.get();
                if (name != null && !name.isBlank() && !name.equalsIgnoreCase(it.getName())) {
                    it.setName(name);
                    return issueTypeRepository.save(it);
                }
                return it;
            }
        }

        // 2) Fallback: lookup by name
        if (name != null && !name.isBlank()) {
            var byName = issueTypeRepository.findByNameIgnoreCase(name);
            if (byName.isPresent()) {
                var it = byName.get();
                // Optional: backfill Jira id if we have it now
                if (jiraId != null && !jiraId.isBlank()
                        && (it.getJiraId() == null || it.getJiraId().isBlank())) {
                    it.setJiraId(jiraId);
                    return issueTypeRepository.save(it);
                }
                return it;
            }
        }

        // 3) Create new (DB id auto-generated)
        var created = IssueType.builder()
                .jiraId((jiraId == null || jiraId.isBlank()) ? null : jiraId)
                .name((name == null || name.isBlank()) ? "Unknown" : name)
                .build();
        return issueTypeRepository.save(created);
    }

    @Override
    @Transactional
    public CreateIssueTypeResponse createIssueType(CreateIssueTypeRequest request) {
        if (request == null) throw new IllegalArgumentException("Request cannot be null");
        if (isBlank(request.getName())) throw new IllegalArgumentException("Issue type name is required");

        String issueTypeUrl = jiraUrlBuilder.url(credentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE);
        log.info("Create issue type at {}: {}", issueTypeUrl, request);

        CreateIssueTypeResponse jiraResponse;
        try {
            jiraResponse = jiraClientConfiguration.post(issueTypeUrl, request, CreateIssueTypeResponse.class, credentials.username(), credentials.token());
            log.info("Created issue type in Jira: {} (id={})", jiraResponse.getName(), jiraResponse.getId());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT && e.getResponseBodyAsString().toLowerCase().contains("already exists")) {

                log.warn("Issue type '{}' already exists; resolving existing from Jira", request.getName());

                CreateIssueTypeResponse[] all =
                        jiraClientConfiguration.get(issueTypeUrl, CreateIssueTypeResponse[].class, credentials.username(), credentials.token());

                jiraResponse = Arrays.stream(all == null ? new CreateIssueTypeResponse[0] : all)
                        .filter(t -> request.getName().equalsIgnoreCase(t.getName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Conflict reported but could not find existing issue type by name"));
            } else {
                throw new CustomException("Error while creating issue type: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        Long id = parseId(jiraResponse.getId());
        assert id != null;
        IssueType entity = issueTypeRepository.findById(id)
                .orElseGet(() -> IssueType.builder().id(id).build());

        mapFromJiraResponse(jiraResponse, entity);
        issueTypeRepository.saveAndFlush(entity);

        return jiraResponse;
    }

    @Override
    public Optional<IssueType> find(IssueTypeReference reference) {
        if (reference.getId() != null) {
            return issueTypeRepository.findById(reference.getId());
        } else if (!isBlank(reference.getName())) {
            return issueTypeRepository.findByName(reference.getName());
        }
        throw new IllegalArgumentException("IssueTypeReference must contain either id or name");
    }

    @Override
    public Optional<IssueType> findById(Long id) { return issueTypeRepository.findById(id); }

    @Override
    public Optional<IssueType> findByName(String name) { return issueTypeRepository.findByName(name); }

    @Override
    public IssueType findOrCreateIssueType(IssueType jiraIssueType, String issueKey) {
        if (jiraIssueType == null || isBlank(String.valueOf(jiraIssueType.getId()))) return null;

        Long id = parseId(String.valueOf(jiraIssueType.getId()));
        assert id != null;

        return issueTypeRepository.findById(id).orElseGet(() -> {
            IssueType e = new IssueType();
            e.setId(jiraIssueType.getId());
            e.setName(jiraIssueType.getName());
            e.setDescription(jiraIssueType.getDescription());
            e.setIconUrl(jiraIssueType.getIconUrl());
            e.setSelf(jiraIssueType.getSelf());
            e.setSubtask(jiraIssueType.getSubtask());
            e.setHierarchyLevel(jiraIssueType.getHierarchyLevel());
            e.setAvatarId(jiraIssueType.getAvatarId());

            String projectKey = "";
            if (issueKey != null && issueKey.contains("-")) {
                projectKey = issueKey.substring(0, issueKey.indexOf('-')).trim();
            }
            try { e.setId(Long.valueOf(projectKey)); } catch (NoSuchMethodError | Exception ignored) {}

            return issueTypeRepository.save(e);
        });
    }

    @Override
    @Transactional
    public IssueType findOrCreateIssueTypeByName(String name) {
        Optional<IssueType> localType = issueTypeRepository.findByName(name);
        if (localType.isPresent()) {
            return localType.get();
        }

        syncAllIssueTypes();

        return issueTypeRepository.findByName(name).orElse(null);
    }

    @Override
    @Transactional
    public List<IssueType> syncAllIssueTypes() {

        String url = jiraUrlBuilder.url(credentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE);
        CreateIssueTypeResponse[] jiraTypes =
                jiraClientConfiguration.get(url, CreateIssueTypeResponse[].class,
                        credentials.username(), credentials.token());

        if (jiraTypes == null || jiraTypes.length == 0) {
            log.info("Fetched 0 issue types from Jira");
            return List.of();
        }

        List<IssueType> out = new ArrayList<>(jiraTypes.length);
        for (CreateIssueTypeResponse jt : jiraTypes) {
            Long id = parseId(jt.getId());
            assert id != null;
            IssueType entity = issueTypeRepository.findById(id)
                    .orElseGet(() -> IssueType.builder().id(id).build());

            mapFromJiraResponse(jt, entity);
            out.add(issueTypeRepository.save(entity));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueType> getAllIssueTypesLocal() {
        return issueTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueType> getAllIssueTypesJira() {
        String url = jiraUrlBuilder.url(credentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE);
        CreateIssueTypeResponse[] arr = jiraClientConfiguration.get(url, CreateIssueTypeResponse[].class,
                credentials.username(), credentials.token());

        List<IssueType> converted = new ArrayList<>(arr.length);
        for (CreateIssueTypeResponse jt : arr) {
            IssueType tmp = new IssueType();
            mapFromJiraResponse(jt, tmp);
            converted.add(tmp);
        }
        return converted;
    }

    @Override
    @Transactional(readOnly = true)
    public IssueType getIssueTypeById(Long issueTypeId) {
        return issueTypeRepository.findById(issueTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("IssueType not found: " + issueTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public IssueType getIssueTypeByName(String issueTypeName) {
        return issueTypeRepository.findByName(issueTypeName)
                .orElseThrow(() -> new ResourceNotFoundException("IssueType not found: " + issueTypeName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueType> getAllIssueTypesForProject(Long projectId) {

        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(credentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE_PROJECT))
                .queryParam("projectId", projectId)
                .toUriString();

        CreateIssueTypeResponse[] arr = jiraClientConfiguration.get(url, CreateIssueTypeResponse[].class, credentials.username(), credentials.token());

        if (arr == null) return List.of();

        List<IssueType> converted = new ArrayList<>(arr.length);
        for (CreateIssueTypeResponse jt : arr) {
            IssueType tmp = new IssueType();
            mapFromJiraResponse(jt, tmp);
            converted.add(tmp);
        }
        return converted;
    }

    @Override
    @Transactional
    public CreateIssueTypeResponse updateIssueType(Long issueTypeId, CreateIssueTypeRequest request) {

        String url = String.format("%s/%s", jiraUrlBuilder.url(credentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE), issueTypeId);

        CreateIssueTypeResponse updated =
                jiraClientConfiguration.put(url, request, CreateIssueTypeResponse.class, credentials.username(), credentials.token());
        log.info("Updated issue type in Jira: {} (id={})", updated.getName(), updated.getId());

        Long id = parseId(updated.getId());
        assert id != null;
        issueTypeRepository.findById(id).ifPresentOrElse(entity -> {
            mapFromJiraResponse(updated, entity);
            issueTypeRepository.save(entity);
        }, () -> {
            IssueType entity = IssueType.builder().id(id).build();
            mapFromJiraResponse(updated, entity);
            issueTypeRepository.save(entity);
        });

        return updated;
    }

    @Override
    @Transactional
    public void deleteIssueType(Long issueTypeId) {
        if (issueTypeId == null) throw new IllegalArgumentException("issueTypeId is required");
        String url = String.format("%s/%s", jiraUrlBuilder.url(credentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE), issueTypeId);
        jiraClientConfiguration.delete(url, Void.class, credentials.username(), credentials.token());
        log.info("Deleted issue type {} in Jira", issueTypeId);

        if (issueTypeRepository.existsById(issueTypeId)) {
            issueTypeRepository.deleteById(issueTypeId);
            log.info("Deleted issue type {} from local DB", issueTypeId);
        }
    }

    @Override
    @Transactional
    public IssueType saveIfNotExistsOrUpdate(IssueType issueType) {
        if (issueType == null || isBlank(String.valueOf(issueType.getId()))) {
            log.warn("Attempted to save/update IssueType with missing Jira ID. Skipping persistence for: {}", issueType);
            return null;
        }

        Long jiraId = parseId(String.valueOf(issueType.getId()));
        if (jiraId == null) {
            log.warn("Attempted to save/update IssueType with non-numeric Jira ID: {}. Skipping.", issueType.getId());
            return null;
        }

        Optional<IssueType> existing = issueTypeRepository.findById(jiraId);
        IssueType managedEntity;
        if (existing.isPresent()) {
            managedEntity = existing.get();
            issueTypeMapper.copyInto(issueTypeMapper.toSummary(issueType), managedEntity);
        } else {
            managedEntity = issueType;
        }

        return issueTypeRepository.save(managedEntity);
    }


    /// //////////////////////
    // Helper
    /// //////////////
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private Long parseId(String jiraId) {
        try {
            return jiraId == null ? null : Long.valueOf(jiraId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Jira issue type id is not numeric: " + jiraId);
        }
    }
    private void mapFromJiraResponse(CreateIssueTypeResponse response, IssueType target) {

        Long id = parseId(response.getId());
        if (target.getId() == null) target.setId(id);

        target.setName(response.getName());
        target.setDescription(response.getDescription());
        target.setIconUrl(response.getIconUrl());
        target.setSelf(response.getSelf());
        target.setSubtask(Boolean.TRUE.equals(response.getSubtask()));
        target.setHierarchyLevel(response.getHierarchyLevel());
        target.setAvatarId(response.getAvatarId() == null ? null : response.getAvatarId());

        String projectKey = extractProjectKeyFromResponse(response);
        if (projectKey == null) projectKey = "";
        try { target.setId(Long.valueOf(projectKey)); } catch (NoSuchMethodError | Exception ignored) { }
    }
    private String extractProjectKeyFromResponse(CreateIssueTypeResponse resp) {
        try {
            if (resp == null || resp.getScope() == null || resp.getScope().getProject() == null) return null;
            return trimToNull(resp.getScope().getProject().getKey());
        } catch (Exception ignored) {
            return null;
        }
    }

    @Transactional
    public IssueType findOrCreate(IssueTypeSummary dto) {
        String jiraId = String.valueOf(dto.getId()); // Jira’s id as string (don’t parse to Long)
        String name   = dto.getName();

        return issueTypeRepository.findByJiraId(jiraId)
                .orElseGet(() -> issueTypeRepository.findByNameIgnoreCase(name)
                        .map(existing -> {
                            // backfill missing fields
                            if (existing.getJiraId() == null) existing.setJiraId(jiraId);
                            existing.setDescription(dto.getDescription());
                            existing.setIconUrl(dto.getIconUrl());
                            existing.setSubtask(dto.getSubtask());
                            if (dto.getAvatarId() != null) existing.setAvatarId(dto.getAvatarId().intValue());
                            existing.setHierarchyLevel(dto.getHierarchyLevel());
                            existing.setSelf(dto.getSelf());
                            return issueTypeRepository.save(existing);
                        })
                        .orElseGet(() -> {
                            IssueType it = IssueType.builder()
                                    .jiraId(jiraId)
                                    .name(name)
                                    .description(dto.getDescription())
                                    .iconUrl(dto.getIconUrl())
                                    .subtask(dto.getSubtask())
                                    .avatarId(dto.getAvatarId() == null ? null : dto.getAvatarId())
                                    .hierarchyLevel(dto.getHierarchyLevel())
                                    .self(dto.getSelf())
                                    .build();
                            return issueTypeRepository.save(it);
                        })
                );
    }



}