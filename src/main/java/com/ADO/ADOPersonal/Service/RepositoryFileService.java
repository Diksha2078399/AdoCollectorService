package com.ADO.ADOPersonal.Service;



import io.mcp.client.MCPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepositoryFileService {

    @Autowired
    private MCPService mcpService;

    public List<String> listFiles(String repoName, String path) throws IOException {
        Path repoPath = Paths.get(mcpService.getRepositoryPath(), repoName, path);
        return Files.walk(repoPath, 1)
                .filter(p -> !p.equals(repoPath))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public String readFile(String repoName, String filePath) throws IOException {
        Path fullPath = Paths.get(mcpService.getRepositoryPath(), repoName, filePath);
        return new String(Files.readAllBytes(fullPath));
    }

    public void writeFile(String repoName, String filePath, String content) throws IOException {
        Path fullPath = Paths.get(mcpService.getRepositoryPath(), repoName, filePath);
        Files.createDirectories(fullPath.getParent());
        Files.write(fullPath, content.getBytes());
    }

    public void uploadFile(String repoName, String path, MultipartFile file) throws IOException {
        Path targetPath = Paths.get(mcpService.getRepositoryPath(), repoName, path, file.getOriginalFilename());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteFile(String repoName, String filePath) throws IOException {
        Path fullPath = Paths.get(mcpService.getRepositoryPath(), repoName, filePath);
        Files.deleteIfExists(fullPath);
    }
}
