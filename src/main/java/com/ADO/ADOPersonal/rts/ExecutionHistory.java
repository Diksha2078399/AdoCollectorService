package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "TestCaseExecutionHistory")
public class ExecutionHistory {

    private String id;
    private String execDate;
    private String projectid;
    private String testCaseID;
    private String release;
    private String status;
    private String testedBy;

    @Transient
    private String type;
}
