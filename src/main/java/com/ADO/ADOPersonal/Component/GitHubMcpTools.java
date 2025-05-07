package com.ADO.ADOPersonal.Component;


import com.ADO.ADOPersonal.Client.AdoClient;
import com.ADO.ADOPersonal.Service.GitHubService;
import com.ADO.ADOPersonal.rts.Defect;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.mcp.server.api.McpServerExchange;
import io.modelcontextprotocol.mcp.server.api.McpServerFeatures;
import io.modelcontextprotocol.mcp.server.api.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GitHubMcpTools {

    private final GitHubService gitHubService;
    private final AdoClient adoClient;
    private final ObjectMapper objectMapper;

    public GitHubMcpTools(GitHubService gitHubService, AdoClient adoClient, ObjectMapper objectMapper) {
        this.gitHubService = gitHubService;
        this.adoClient = adoClient;
        this.objectMapper = objectMapper;
    }

    @McpServerFeatures.SyncToolSpecification(
            tool = @Tool(
                    id = "list_repositories",
                    description = "Lists all repositories for a given GitHub username.",
                    schema = "{\"type\":\"object\",\"properties\":{\"username\":{\"type\":\"string\",\"description\":\"GitHub username\"}},\"required\":[\"username\"]}"
            )
    )
    public McpServerFeatures.CallToolResult listRepositories(McpServerExchange exchange, Map<String, Object> arguments) {
        String username = (String) arguments.get("username");
        List<Map<String, Object>> repos = gitHubService.listRepositories(username);
        return new McpServerFeatures.CallToolResult(repos, true);
    }

    @McpServerFeatures.SyncToolSpecification(
            tool = @Tool(
                    id = "create_repository",
                    description = "Creates a new repository for the authenticated user.",
                    schema = "{\"type\":\"object\",\"properties\":{\"username\":{\"type\":\"string\",\"description\":\"GitHub username\"},\"repoName\":{\"type\":\"string\",\"description\":\"Name of the repository\"},\"description\":{\"type\":\"string\",\"description\":\"Description of the repository\"}},\"required\":[\"username\",\"repoName\"]}"
            )
    )
    public McpServerFeatures.CallToolResult createRepository(McpServerExchange exchange, Map<String, Object> arguments) {
        String username = (String) arguments.get("username");
        String repoName = (String) arguments.get("repoName");
        String description = arguments.get("description") != null ? (String) arguments.get("description") : "";
        Map<String, Object> repo = gitHubService.createRepository(username, repoName, description);
        return new McpServerFeatures.CallToolResult(repo, true);
    }

    @McpServerFeatures.SyncToolSpecification(
            tool = @Tool(
                    id = "read_file",
                    description = "Reads the content of a file in a GitHub repository.",
                    schema = "{\"type\":\"object\",\"properties\":{\"owner\":{\"type\":\"string\",\"description\":\"Repository owner\"},\"repo\":{\"type\":\"string\",\"description\":\"Repository name\"},\"path\":{\"type\":\"string\",\"description\":\"File path in the repository\"}},\"required\":[\"owner\",\"repo\",\"path\"]}"
            )
    )
    public McpServerFeatures.CallToolResult readFile(McpServerExchange exchange, Map<String, Object> arguments) {
        String owner = (String) arguments.get("owner");
        String repo = (String) arguments.get("repo");
        String path = (String) arguments.get("path");
        String content = gitHubService.readFile(owner, repo, path);
        return new McpServerFeatures.CallToolResult(content, true);
    }

    @McpServerFeatures.SyncToolSpecification(
            tool = @Tool(
                    id = "write_file",
                    description = "Writes content to a file in a GitHub repository.",
                    schema = "{\"type\":\"object\",\"properties\":{\"owner\":{\"type\":\"string\",\"description\":\"Repository owner\"},\"repo\":{\"type\":\"string\",\"description\":\"Repository name\"},\"path\":{\"type\":\"string\",\"description\":\"File path in the repository\"},\"content\":{\"type\":\"string\",\"description\":\"File content\"},\"commitMessage\":{\"type\":\"string\",\"description\":\"Commit message\"}},\"required\":[\"owner\",\"repo\",\"path\",\"content\",\"commitMessage\"]}"
            )
    )
    public McpServerFeatures.CallToolResult writeFile(McpServerExchange exchange, Map<String, Object> arguments) {
        String owner = (String) arguments.get("owner");
        String repo = (String) arguments.get("repo");
        String path = (String) arguments.get("path");
        String content = (String) arguments.get("content");
        String commitMessage = (String) arguments.get("commitMessage");
        Map<String, Object> result = gitHubService.writeFile(owner, repo, path, content, commitMessage);
        return new McpServerFeatures.CallToolResult(result, true);
    }

    @McpServerFeatures.SyncToolSpecification(
            tool = @Tool(
                    id = "save_defects_to_repo",
                    description = "Fetches defects from ADO and saves them as a JSON file in a GitHub repository.",
                    schema = "{\"type\":\"object\",\"properties\":{\"owner\":{\"type\":\"string\",\"description\":\"Repository owner\"},\"repo\":{\"type\":\"string\",\"description\":\"Repository name\"},\"path\":{\"type\":\"string\",\"description\":\"File path in the repository\"},\"jiraProjectKey\":{\"type\":\"string\",\"description\":\"ADO project key\"},\"token\":{\"type\":\"string\",\"description\":\"ADO API token\"}},\"required\":[\"owner\",\"repo\",\"path\",\"jiraProjectKey\",\"token\"]}"
            )
    )
    public McpServerFeatures.CallToolResult saveDefectsToRepo(McpServerExchange exchange, Map<String, Object> arguments) {
        String owner = (String) arguments.get("owner");
        String repo = (String) arguments.get("repo");
        String path = (String) arguments.get("path");
        String jiraProjectKey = (String) arguments.get("jiraProjectKey");
        String token = (String) arguments.get("token");

        List<Defect> defects = (List<Defect>) adoClient.fetchWorkItemsFromADO("Bug", null, Defect.class, jiraProjectKey, token);
        try {
            String jsonContent = objectMapper.writeValueAsString(defects);
            Map<String, Object> result = gitHubService.writeFile(owner, repo, path, jsonContent, "Save ADO defects");
            return new McpServerFeatures.CallToolResult(result, true);
        } catch (Exception e) {
            return new McpServerFeatures.CallToolResult("Failed to serialize defects: " + e.getMessage(), false);
        }
    }
}