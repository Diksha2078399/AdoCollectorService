package com.ADO.ADOPersonal.controller;

import com.ADO.ADOPersonal.Component.AdoComponent;
import com.ADO.ADOPersonal.Service.AdoService;
import com.ADO.ADOPersonal.rts.Defect;
import com.ADO.ADOPersonal.rts.TestCaseRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ado")
public class ADOController {
    @Autowired
    private AdoService adoService;

    @Autowired
    private AdoComponent adoComponent;


    @GetMapping("/fetch-data")
    public CompletableFuture<ResponseEntity<String>> fetchData(@RequestParam("projectId") String projectId, @RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) throws JsonProcessingException {
        System.out.println("FetchData endpoint called with projectId: " + projectId);
        return adoComponent.getIssuesAsync("ADO", projectId, jiraProjectKey, token)
                .thenApply(result -> {
                    System.out.println("Data fetching completed successfully.");
                    return ResponseEntity.ok("Data fetched and saved successfully!");
                })
                .exceptionally(ex -> {
                    System.err.println("Error during data fetching: " + ex.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("An error occurred while fetching data.");
                });
    }

    @PostMapping("/update-userstory")
    public ResponseEntity<String> updateUserStory(@RequestParam("id") String id,@RequestParam("projectId") String projectId,@RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token, @RequestBody Map<String, Object> updateFields) {
        try {
            adoService.updateUserStory(id, projectId,jiraProjectKey,token,updateFields);
            return ResponseEntity.ok("User story updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the user story: " + e.getMessage());
        }
    }


    @PostMapping("/create-testcase")
    public ResponseEntity<String> createTestCase(@RequestBody TestCaseRequest request, @RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            String result = adoService.createTestCase(request,jiraProjectKey,token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the test case: " + e.getMessage());
        }
    }

    @PutMapping("/update-testcase/{id}")
    public ResponseEntity<String> updateTestCase(@PathVariable("id") String id, @RequestBody TestCaseRequest request, @RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            String result = adoService.updateTestCase(id, request, jiraProjectKey, token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the test case: " + e.getMessage());
        }
    }

    @GetMapping("/defect-images/{id}")
    public ResponseEntity<List<byte[]>> getDefectImages(@PathVariable("id") String id,@RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            List<byte[]> imageBytesList = adoService.getDefectImages(id, jiraProjectKey, token);
            if (!imageBytesList.isEmpty()) {

                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+imageBytesList);
                return ResponseEntity.ok().body(imageBytesList);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update-defect/{id}")
    public ResponseEntity<String> updateDefect(@PathVariable("id") String id, @RequestBody Defect request, @RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            String result = adoService.updateDefect(id, request, jiraProjectKey, token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the defect: " + e.getMessage());
        }
    }

    @GetMapping("/fetch-users")
    public ResponseEntity<Map<String, String>> fetchUsers(@RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            Map<String, String> users = adoService.fetchUsers(jiraProjectKey, token);
            System.out.println(users);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/fetch-work-item-types")
    public ResponseEntity<String> fetchUniqueWorkItemTypes(@RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            String templateTypeId = adoService.fetchUniqueWorkItemTypes(jiraProjectKey, token);
            return ResponseEntity.ok(templateTypeId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/fetch-workitem-templateTypeId")
    public ResponseEntity<List<String>> fetchWorkItemTypes(@RequestParam("jiraProjectKey") String jiraProjectKey, @RequestHeader("authorization") String token) {
        try {
            List<String> workItemNames = adoService.fetchWorkItemTypes(jiraProjectKey, token);
            return ResponseEntity.ok(workItemNames);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

