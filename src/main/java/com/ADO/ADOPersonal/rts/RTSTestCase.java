package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "testcase")
public class RTSTestCase {
    @Id
    private String _id;
    private String testCaseID ;
    private String description;
    private String module ;
    private String priority ;
    private String projectid ;
    private String createdOn ;
    private String scriptName ;
    private String linkedUserstories ;
    private String testCaseType ;
    private String release ;
    private String sprint ;
    private String testCaseName;
    private List<String> testStep;
    private List<String> expectedResult;
    private String jiraProjectKey;
    private String lastUpdated;
    private boolean businessCritical;

    @Transient
    private String type;

    @Transient
    private String fetchType;
}