package com.ADO.ADOPersonal.rts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "test_execution_history")
@Data
public class TestExecutionHistory {
    @Id
    private String id;
    private String testCaseId;
    private String projectId;
    private String jiraProjectKey;
    private String jiraProjectId;
    private String testSuiteId;
    private String testSuiteName;
    private String testcaseExecutionId;
    private String testExecutionStatus;
    private String lastUpdated;

//    private List executionRuns;
//    private List testCycleKeys;
//    private List testCaseExecutionStatus;
}

