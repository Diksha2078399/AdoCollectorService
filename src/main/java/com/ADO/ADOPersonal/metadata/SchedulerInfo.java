package com.ADO.ADOPersonal.metadata;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("scheduler_runs")
public class SchedulerInfo {

    @Id
    private String id;
    private String lastUpdatedDate;
    private String desc;
    private String toolName;
    private String projectId;
    private String status;

    private int defectCount;
    private int testCaseCount;
    private int userStoryCount;
    private int testExecutionCount;

    private String jiraProjectKey;

}
