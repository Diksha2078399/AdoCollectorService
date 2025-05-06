package com.ADO.ADOPersonal.controller;

package com.ADO.ADOPersonal.controller;

import com.ADO.ADOPersonal.service.RepositoryFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/mcp/files")
public class RepositoryFileController {

    @Autowired
    private RepositoryFileService fileService;

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles(
            @RequestParam String repoName,
            @RequestParam(defaultValue = "") String path) {
        try {
            return ResponseEntity.ok(fileService.listFiles(repoName, path));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/read")
    public ResponseEntity<String> readFile(
            @RequestParam String repoName,
            @RequestParam String filePath) {
        try {
            return ResponseEntity.ok(fileService.readFile(repoName, filePath));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error reading file: " + e.getMessage());
        }
    }

    @PostMapping("/write")
    public ResponseEntity<String> writeFile(
            @RequestParam String repoName,
            @RequestParam String filePath,
            @RequestBody String content) {
        try {
            fileService.writeFile(repoName, filePath, content);
            return ResponseEntity.ok("File written successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error writing file: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam String repoName,
            @RequestParam String path,
            @RequestParam("file") MultipartFile file) {
        try {
            fileService.uploadFile(repoName, path, file);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(
            @RequestParam String repoName,
            @RequestParam String filePath) {
        try {
            fileService.deleteFile(repoName, filePath);
            return ResponseEntity.ok("File deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error deleting file: " + e.getMessage());
        }
    }
}
