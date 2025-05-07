package com.ADO.ADOPersonal.Component;



import com.ADO.ADOPersonal.Service.GitHubService;
import io.modelcontextprotocol.mcp.server.api.McpTool;
import io.modelcontextprotocol.mcp.server.api.McpToolContext;
import io.modelcontextprotocol.mcp.server.api.McpToolResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GitHubMcpTools {

    private final GitHubService gitHubService;

    public GitHubMcpTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @McpTool(
            id = "list_repositories",
            description = "Lists all repositories for a given GitHub username.",
            parameters = {
                    @McpTool.Parameter(name = "username", description = "GitHub username", required = true)
            }
    )
    public McpToolResult listRepositories(McpToolContext context) {
        String username = context.getParameter("username").getAsString();
        List<Map<String, Object>> repos = gitHubService.listRepositories(username);
        return McpToolResult.builder()
                .result(repos)
                .build();
    }

    @McpTool(
            id = "create_repository",
            description = "Creates a new repository for the authenticated user.",
            parameters = {
                    @McpTool.Parameter(name = "username", description = "GitHub username", required = true),
                    @McpTool.Parameter(name = "repoName", description = "Name of the repository", required = true),
                    @McpTool.Parameter(name = "description", description = "Description of the repository", required = false)
            }
    )
    public McpToolResult createRepository(McpToolContext context) {
        String username = context.getParameter("username").getAsString();
        String repoName = context.getParameter("repoName").getAsString();
        String description = context.getParameter("description") != null ? context.getParameter("description").getAsString() : "";
        Map<String, Object> repo = gitHubService.createRepository(username, repoName, description);
        return McpToolResult.builder()
                .result(repo)
                .build();
    }

    @McpTool(
            id = "read_file",
            description = "Reads the content of a file in a GitHub repository.",
            parameters = {
                    @McpTool.Parameter(name = "owner", description = "Repository owner", required = true),
                    @McpTool.Parameter(name = "repo", description = "Repository name", required = true),
                    @McpTool.Parameter(name = "path", description = "File path in the repository", required = true)
            }
    )
    public McpToolResult readFile(McpToolContext context) {
        String owner = context.getParameter("owner").getAsString();
        String repo = context.getParameter("repo").getAsString();
        String path = context.getParameter("path").getAsString();
        String content = gitHubService.readFile(owner, repo, path);
        return McpToolResult.builder()
                .result(content)
                .build();
    }

    @McpTool(
            id = "write_file",
            description = "Writes content to a file in a GitHub repository.",
            parameters = {
                    @McpTool.Parameter(name = "owner", description = "Repository owner", required = true),
                    @McpTool.Parameter(name = "repo", description = "Repository name", required = true),
                    @McpTool.Parameter(name = "path", description = "File path in the repository", required = true),
                    @McpTool.Parameter(name = "content", description = "File content", required = true),
                    @McpTool.Parameter(name = "commitMessage", description = "Commit message", required = true)
            }
    )
    public McpToolResult writeFile(McpToolContext context) {
        String owner = context.getParameter("owner").getAsString();
        String repo = context.getParameter("repo").getAsString();
        String path = context.getParameter("path").getAsString();
        String content = context.getParameter("content").getAsString();
        String commitMessage = context.getParameter("commitMessage").getAsString();
        Map<String, Object> result = gitHubService.writeFile(owner, repo, path, content, commitMessage);
        return McpToolResult.builder()
                .result(result)
                .build();
    }
}
