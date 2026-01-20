package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.pw.edu.pl.master.thesis.issues.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.issues.configuration.JiraWebClientConfig;
import com.pw.edu.pl.master.thesis.issues.configuration.RequestCredentials;
import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.request.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeSummary;
import com.pw.edu.pl.master.thesis.issues.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.issues.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.issues.mapper.JiraIssueMapper;
import com.pw.edu.pl.master.thesis.issues.service.JiraIssueService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraIssueServiceImplementation implements JiraIssueService {

    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final RequestCredentials requestCredentials;
    private final WebClient jiraWebClient;

    @Override
    public IssueResponseSummary createIssue(CreateIssueRequest request) {
        String issueSummary = request.getFields().getSummary();
        if (issueSummary == null || issueSummary.isBlank()) {
            throw new IllegalArgumentException("Issue summary is null or empty");
        }

        JQLIssueSummary jqlIssueSummary = new JQLIssueSummary();
        jqlIssueSummary.setSummary(issueSummary);
        jqlIssueSummary.setMaxResults(1000);
        jqlIssueSummary.setReconcileIssues(new ArrayList<>());
        jqlIssueSummary.setFieldsByKeys(true);

        JqlSearchResponse jqlSearchResponse = searchIssuesByJqlPostSummaryBody(jqlIssueSummary);

        int  issueCount = jqlSearchResponse.getIssues().size();

        if (issueCount == 0) {
            String url = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE);
            return jiraClientConfiguration.post(url, request, IssueResponseSummary.class, requestCredentials.username(), requestCredentials.token());
        }else {
            throw new IllegalArgumentException("Issue with this summary already exists :  " + issueSummary);
        }
    }

    @Override
    @Transactional
    public IssueResponseList bulkCreateOrUpdateIssues(BulkCreateOrUpdateIssues bulkRequest) {
        if (bulkRequest == null || bulkRequest.getCreateOrUpdateIssues() == null
                || bulkRequest.getCreateOrUpdateIssues().isEmpty()) {
            throw new IllegalArgumentException("Request must include a non-empty `issues` list");
        }

        List<IssueResponse> out = new ArrayList<>();

        for (CreateIssueRequest createReq : bulkRequest.getCreateOrUpdateIssues()) {
            IssueResponseSummary summary = createIssue(createReq);
            String getUrl = UriComponentsBuilder
                    .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                    .pathSegment(summary.getKey())
                    .toUriString();

            IssueResponse jira = jiraClientConfiguration.get( getUrl, IssueResponse.class,
                    requestCredentials.username(), requestCredentials.token() );

            // 3) Skip local database synchronization (upsert status, issuetype, issue entity)
            // The original code here is REMOVED to prevent the DataIntegrityViolationException.

            out.add(jira);
        }

        return IssueResponseList.builder().issues(out).build();
    }

    @Override
    public List<IssueTypeSummary> listAllIssueTypes() {
        String url = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE);
        IssueTypeSummary[] issueTypeSummaries = jiraClientConfiguration.get(url, IssueTypeSummary[].class, requestCredentials.username(), requestCredentials.token());
        return issueTypeSummaries == null ? List.of() : Arrays.asList(issueTypeSummaries);
    }

    @Override
    public IssueResponse getIssueByKeyJira(String issueKey) {
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(),
                        JiraApiEndpoint.ISSUE))
                .pathSegment(issueKey)
                .toUriString();
        return jiraClientConfiguration.get(url, IssueResponse.class , requestCredentials.username(), requestCredentials.token());
    }


    @Override
    public IssueResponse getIssueByIdOrKey(String issueKey){
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueKey)
                .toUriString();

        log.info("Url: {}", url);

        return jiraClientConfiguration.get(url, IssueResponse.class , requestCredentials.username(), requestCredentials.token());

    }

    @Override
    public IssueResponse getIssueById(int issueId) {

        if (issueId <= 0) {
            throw new IllegalArgumentException("issueId must be greater than 0");
        }

        String url = UriComponentsBuilder.fromUriString(
                jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE ))
                .pathSegment(String.valueOf(issueId))
                .toUriString();

        log.info("Url: {}", url);

        return jiraClientConfiguration.get(
                url,
                IssueResponse.class,
                requestCredentials.username(),
                requestCredentials.token()
        );
    }


    public List<IssueTypeSummary> getAllJiraIssueForProject(String projectId) {
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE_TYPE_PROJECT))
                .queryParam("projectId", projectId)
                .toUriString();
        IssueTypeSummary[] arr = jiraClientConfiguration.get(url, IssueTypeSummary[].class, requestCredentials.username(), requestCredentials.token());
        return arr == null ? List.of() : Arrays.asList(arr);
    }

    @Override
    public JqlSearchResponse searchIssuesByJqlPostSummary(String issueSummary, String projectKey) {
        if (projectKey == null || projectKey.isBlank()) {
            throw new IllegalArgumentException("projectKey must not be blank");
        }
        if (issueSummary == null || issueSummary.isBlank()) {
            throw new IllegalArgumentException("issueSummary must not be blank");
        }

        // Build safe JQL
        String safeProject = quoteForJql(projectKey);          // "ABC"
        String safeSummary = quoteForJql(issueSummary);        // "Login failed on mobile"

        String jql = String.format("project = %s AND summary ~ %s", safeProject, safeSummary);

        JqlSearchRequest jqlSearchRequest = JqlSearchRequest.builder()
                .jql(jql)
                .maxResults(1000)
                .nextPageToken(null)
                .expand("changelog,renderedFields,comments")
                .fieldsByKeys(true)
                .fields(jiraUrlBuilder.getJiraFields())
                .reconcileIssues(new ArrayList<>())
                .build();

        return this.searchIssuesByJqlPost(jqlSearchRequest);
    }

    /** Wrap with double-quotes and escape Jira JQL specials inside the string literal. */
    private static String quoteForJql(String s) {
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }


    public JqlSearchResponse searchIssuesByJqlPost(JqlSearchRequest request) {
        if (request == null || request.getJql() == null || request.getJql().isBlank()) {
            throw new IllegalArgumentException("JQL query must be provided in the request body.");
        }
        String url = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.SEARCH_JQL);

        return jiraClientConfiguration.post(url, request, JqlSearchResponse.class,
                requestCredentials.username(), requestCredentials.token());
    }


    @Override
    public IssueResponse getIssueByKeyIssuesSummaryResponse(String issueKey){
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueKey)
                .toUriString();
        return jiraClientConfiguration.get(url, IssueResponse.class , requestCredentials.username(), requestCredentials.token());
    }

    @Override
    public IssueResponse getIssueWithSelectedFields(String issueKey, String fieldsCsv) {
        if (issueKey == null || issueKey.isBlank()) {
            throw new IllegalArgumentException("issueKey is required");
        }

        String fields = (fieldsCsv == null || fieldsCsv.isBlank())
                ? "attachment"
                : fieldsCsv;

        // build FULL Jira URL using your helper
        // e.g. https://bugresolution.atlassian.net/rest/api/3/issue
        String baseIssueUrl = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE);

        // now add /{key} and ?fields=...
        String fullUrl = baseIssueUrl + "/" + issueKey + "?fields=" + fields;

        return jiraWebClient
                .get()
                .uri(fullUrl)   // pass FULL url → config will see it's absolute → won't prepend
                .retrieve()
                .bodyToMono(IssueResponse.class)
                .block();
    }




    @Override
    public IssueResponseList bulkFetchIssuesByIdOrKey(GetIssuesListRequest request) {

        List<String> idsOrKeys = request.getIssueIdsOrKeys();
        if (idsOrKeys == null || idsOrKeys.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one issue ID or key");
        }

        List<IssueResponse> issueResponseList = new ArrayList<>();
        for (String idOrKey : idsOrKeys) {
            String url = UriComponentsBuilder
                    .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                    .pathSegment(idOrKey)
                    .toUriString();
            issueResponseList.add(jiraClientConfiguration.get(url, IssueResponse.class , requestCredentials.username(), requestCredentials.token()));
        }
        return IssueResponseList.builder().issues(issueResponseList).build();
    }


    @Override
    public CreateMetaResponse getCreateMeta() {

        String url = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE_CREATEMETA);
        return jiraClientConfiguration.get(url, CreateMetaResponse.class, requestCredentials.username(), requestCredentials.token());
    }

    @Override
    public CreateMetaResponse getCreateMetaForProject(String projectIdOrKey) {

        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE_CREATEMETA))
                .pathSegment(projectIdOrKey, "issuetypes")
                .toUriString();
        return jiraClientConfiguration.get(url, CreateMetaResponse.class, requestCredentials.username(), requestCredentials.token());
    }


    @Override
    @Transactional
    public IssueResponse updateIssue(String issueIdOrKey, CreateIssueRequest request) {

        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueIdOrKey)
                .toUriString();

        // Jira returns 204; don't try to parse a body
        jiraClientConfiguration.put(url, request, Void.class, requestCredentials.username(), requestCredentials.token());

        // Read the latest snapshot
        return jiraClientConfiguration.get(url, IssueResponse.class, requestCredentials.username(), requestCredentials.token());
    }


    @Override
    @Transactional
    public void deleteIssueByKey(String issueIdOrKey) {
        if (issueIdOrKey == null || issueIdOrKey.isBlank()) {
            throw new IllegalArgumentException("Issue key must not be null or blank.");
        }
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueIdOrKey)
                .toUriString();
        jiraClientConfiguration.delete(url, Void.class, requestCredentials.username(), requestCredentials.token());
    }


    @Override
    @Transactional
    public void assignIssue(String issueIdOrKey, String accountId) {

        if (issueIdOrKey == null || issueIdOrKey.isBlank()) {
            throw new IllegalArgumentException("Issue key must not be null or blank.");
        }
        AssigneeRequest dto = new AssigneeRequest(accountId);
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueIdOrKey, "assignee")
                .toUriString();
        jiraClientConfiguration.put(url, dto, Void.class , requestCredentials.username(), requestCredentials.token());
    }

    @Override
    public ChangelogResponse getChangelog(String issueIdOrKey) {

        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueIdOrKey, "changelog")
                .toUriString();
        return jiraClientConfiguration.get(url, ChangelogResponse.class, requestCredentials.username(), requestCredentials.token());
    }

    @Override
    public ChangelogListResponse listChangelog(String issueIdOrKey, ChangelogListRequest request) {
        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(issueIdOrKey, "changelog", "list")
                .toUriString();
        return jiraClientConfiguration.post(url, request, ChangelogListResponse.class , requestCredentials.username(), requestCredentials.token());
    }


    @Override
    public ArchiveResponse archiveIssues(ArchiveRequest request) {
        String url = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE_ARCHIVE);
        return jiraClientConfiguration.put(url, request, ArchiveResponse.class, requestCredentials.username(), requestCredentials.token());
    }


    @Override
    public ArchiveResponse unarchiveIssues(ArchiveRequest request) {
        String url = jiraUrlBuilder.url(requestCredentials.baseUrl() , JiraApiEndpoint.ISSUE_UNARCHIVE);
        return jiraClientConfiguration.put(url, request, ArchiveResponse.class , requestCredentials.username(), requestCredentials.token());
    }

    public List<IssueResponse> getAllIssuesForProject(JqlSearchRequest jqlQuery) {
        JqlSearchResponse response = this.searchIssuesByJqlPost(jqlQuery);
        return new ArrayList<>(response.getIssues());
    }


    @Override
    public CustomFieldResponse createCustomField(CustomFieldRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Field name must be provided.");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException("Field type must be provided.");
        }
        if (request.getSearcherKey() == null || request.getSearcherKey().isBlank()) {
            throw new IllegalArgumentException("Searcher key must be provided.");
        }
        String url = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.FIELD);
        return jiraClientConfiguration.post(url, request, CustomFieldResponse.class ,  requestCredentials.username(), requestCredentials.token());
    }

    @Override
    @Transactional
    public void setCustomFieldToIssue(SetCustomFieldRequest request) {
        if (request == null || request.getIssueKey() == null || request.getCustomFieldId() == null || request.getValue() == null) {
            throw new IllegalArgumentException("Issue key, custom field ID, and value must be provided.");
        }

        Map<String, Object> fields = new HashMap<>();
        fields.put(request.getCustomFieldId(), request.getValue());

        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fields);

        String url = UriComponentsBuilder
                .fromUriString(jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE))
                .pathSegment(request.getIssueKey())
                .toUriString();

        jiraClientConfiguration.put(url, payload, Void.class,  requestCredentials.username(), requestCredentials.token());
    }

    @Override
    public JqlSearchResponse searchIssuesByJqlPostSummaryBody(JQLIssueSummary body) {
        if (body == null) throw new IllegalArgumentException("Body is required");
        // require at least summary or jql
        if ((body.getJql() == null || body.getJql().isBlank())
                && (body.getSummary() == null || body.getSummary().isBlank())) {
            throw new IllegalArgumentException("Provide either 'jql' or 'summary'");
        }
        JqlSearchRequest req = toJqlSearchRequest(body);
        return this.searchIssuesByJqlPost(req);
    }

    private JqlSearchRequest toJqlSearchRequest(JQLIssueSummary body) {
        // defaults
        String jql = (body.getJql() != null && !body.getJql().isBlank())
                ? body.getJql()
                : (body.getSummary() != null && !body.getSummary().isBlank()
                ? "summary ~ " + quoteForJql(body.getSummary()) + " ORDER BY created DESC"
                : "order by created desc"); // fallback JQL if absolutely nothing provided

        int maxResults = (body.getMaxResults() != null && body.getMaxResults() > 0)
                ? body.getMaxResults() : 50;

        String expand = (body.getExpand() == null || body.getExpand().isBlank())
                ? null : body.getExpand();

        List<String> fields = (body.getFields() == null) ? List.of() : body.getFields();
        boolean fieldsByKeys = (body.getFieldsByKeys() != null) ? body.getFieldsByKeys() : true;
        List<String> properties = (body.getProperties() == null) ? List.of() : body.getProperties();
        List<Integer> reconcileIssues = (body.getReconcileIssues() == null) ? List.of() : body.getReconcileIssues();

        return JqlSearchRequest.builder()
                .jql(jql)
                .maxResults(maxResults)
                .nextPageToken(body.getNextPageToken())  // keep if your backend supports it
                .expand(expand)
                .fieldsByKeys(fieldsByKeys)
                .fields(fields.isEmpty() ? jiraUrlBuilder.getJiraFields() : fields)
                .properties(properties)
                .reconcileIssues(reconcileIssues)
                .build();
    }

    public String deleteIssueByKey(String issueKey, boolean deleteSubtasks) {
        String base = jiraUrlBuilder.url(requestCredentials.baseUrl(), JiraApiEndpoint.ISSUE);
        String url = UriComponentsBuilder
                .fromUriString(base)
                .pathSegment(issueKey)
                .queryParam("deleteSubtasks", deleteSubtasks ? "true" : null)
                .toUriString();

        try {
            getIssueByKeyJira(issueKey);
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Issue " + issueKey + " not found or not visible with current credentials.", e);
        }

        try {
            jiraClientConfiguration.delete(url, Void.class, requestCredentials.username(), requestCredentials.token());
            return "Issue deleted: " + issueKey;
        } catch (Exception exception) {
            return "Issue still exists: " + issueKey  +" : " + exception.getMessage();
        }
    }

    @Override
    public List<String> deleteMultipleIssueByKey(DeleteIssuesRequest request) {
        if (request == null || request.getIssueKeys() == null || request.getIssueKeys().isEmpty()) {
            throw new IllegalArgumentException("Issue keys list cannot be empty");
        }

        List<String> results = new ArrayList<>();

        for (String issueKey : request.getIssueKeys()) {
            try {
                deleteIssueByKey(issueKey, true);
                results.add(issueKey + " - DELETED");
            } catch (Exception e) {
                results.add(issueKey + " - FAILED: " + e.getMessage());
            }
        }
        return results;
    }

    private boolean hasText(String string) {
        return string != null && !string.trim().isEmpty();
    }

    private String safe(String string) {
        return hasText(string) ? string.trim() : null;
    }






}