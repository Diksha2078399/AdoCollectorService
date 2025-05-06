package com.ADO.ADOPersonal.controller;



import com.ADO.ADOPersonal.service.MCPService;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;

@RestController
@RequestMapping("/mcp/github")
public class GitHubController {

    @Autowired
    private MCPService mcpService;

    @GetMapping("/repos")
    public ResponseEntity<Collection<GHRepository>> listRepositories() throws IOException {
        GitHub github = mcpService.connectToGitHub();
        return ResponseEntity.ok(github.getMyself().getAllRepositories().values());
    }

    @PostMapping("/commit")
    public ResponseEntity<String> commitAndPush(
            @RequestParam String repoName,
            @RequestParam String message) {
        try {
            // This would require implementing commit/push functionality in MCPService
            mcpService.commitAndPush(repoName, message);
            return ResponseEntity.ok("Changes committed and pushed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error committing changes: " + e.getMessage());
        }
    }

    @PostMapping("/pull")
    public ResponseEntity<String> pullChanges(@RequestParam String repoName) {
        try {
            mcpService.pullChanges(repoName);
            return ResponseEntity.ok("Changes pulled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error pulling changes: " + e.getMessage());
        }
    }
}
