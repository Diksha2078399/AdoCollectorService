package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "defect")
public class Defect {
    private String id;
    private String defid;
    private String projectid;
    private String actualRootcause;
    private String assignedTo;
    private String closedDate;
    private String customLabel1;
    private String description;
    private String detectedBY;
    private String detectedDate;
    private String linkedTestCases;
    private String linkedUserstories;
    private String module;
    private String priority;
    private String release;
    private String resolutionNotes;
    private String severity;
    private String shortDescription;
    private String sprint;
    private String status;
    private String jiraProjectKey;
    private String lastUpdated;
    private boolean isImageContain;
    private List<String> imagePaths;
    @Transient
    private String type;
    @Transient
    private String fetchType;

    private String ReproSteps;

    private String comments;
    private String assigned_To;
    private String systemInfo;
}