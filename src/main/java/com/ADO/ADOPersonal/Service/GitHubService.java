package com.ADO.ADOPersonal.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    private final RestTemplate restTemplate;

    @Value("${github.api.token}")
    private String githubApiToken;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    public GitHubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubApiToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }

    public List<Map<String, Object>> listRepositories(String username) {
        String url = githubApiBaseUrl + "/users/" + username + "/repos";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        return (List<Map<String, Object>>) response.getBody();
    }

    public Map<String, Object> createRepository(String username, String repoName, String description) {
        String url = githubApiBaseUrl + "/user/repos";
        HttpHeaders headers = createHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("name", repoName);
        body.put("description", description);
        body.put("private", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public String readFile(String owner, String repo, String path) {
        String url = githubApiBaseUrl + "/repos/" + owner + "/" + repo + "/contents/" + path;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> content = response.getBody();
        return new String(java.util.Base64.getDecoder().decode((String) content.get("content")));
    }

    public Map<String, Object> writeFile(String owner, String repo, String path, String content, String commitMessage) {
        String url = githubApiBaseUrl + "/repos/" + owner + "/" + repo + "/contents/" + path;
        HttpHeaders headers = createHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("message", commitMessage);
        body.put("content", java.util.Base64.getEncoder().encodeToString(content.getBytes()));
        // Optionally, include 'sha' if updating an existing file

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
        return response.getBody();
    }
}
