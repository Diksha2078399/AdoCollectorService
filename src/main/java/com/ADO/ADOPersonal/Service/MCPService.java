package com.ADO.ADOPersonal.Service;


import io.mcp.client.MCPClient;
import io.mcp.server.MCPServer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MCPService {

    @Autowired
    private MCPServer mcpServer;

    @Value("${github.token}")
    private String githubToken;

    @Value("${mcp.repository.path}")
    private String repositoryPath;

    // Initialize MCP Client
    public MCPClient getMCPClient() {
        return new MCPClient("http://localhost:" + mcpServer.getPort());
    }

    // Clone repository from GitHub
    public String cloneRepository(String repoUrl, String repoName) throws GitAPIException {
        Path repoPath = Paths.get(repositoryPath, repoName);

        // Clone using JGit
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(repoPath.toFile())
                .call();

        // Register with MCP Server
        mcpServer.registerRepository(repoName, repoPath.toString());

        return repoPath.toString();
    }

    // Create new repository
    public String createRepository(String repoName) throws GitAPIException, IOException {
        Path repoPath = Paths.get(repositoryPath, repoName);

        // Initialize local repo
        Git.init().setDirectory(repoPath.toFile()).call();

        // Register with MCP Server
        mcpServer.registerRepository(repoName, repoPath.toString());

        return repoPath.toString();
    }

    // Connect to GitHub
    public GitHub connectToGitHub() throws IOException {
        return new GitHubBuilder().withOAuthToken(githubToken).build();
    }

    // Create GitHub repository and clone it locally
    public String createGitHubRepository(String repoName, String description, boolean isPrivate)
            throws IOException, GitAPIException {
        GitHub github = connectToGitHub();
        GHRepository repo = github.createRepository(repoName)
                .description(description)
                .private_(isPrivate)
                .create();

        return cloneRepository(repo.getHttpTransportUrl(), repoName);
    }

    public void commitAndPush(String repoName, String message) throws GitAPIException, IOException {
        Path repoPath = Paths.get(repositoryPath, repoName);
        try (Git git = Git.open(repoPath.toFile())) {
            // Add all files
            git.add().addFilepattern(".").call();

            // Commit
            git.commit()
                    .setMessage(message)
                    .call();

            // Push
            git.push().call();
        }
    }

    public void pullChanges(String repoName) throws GitAPIException, IOException {
        Path repoPath = Paths.get(repositoryPath, repoName);
        try (Git git = Git.open(repoPath.toFile())) {
            git.pull().call();
        }
    }
}

