package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "userstories")
@Data
public class Userstories {
    @Id
    private String _id;
    private String userStoryID;
    private String description;
    private Date createdOn;
    private String sprint;
    private String team;
    private String author;
    private String owner;
    private String projectid;
    private String customLabel1;
    private String customLabel2;
    private String customLabel3;
    private String similarityPerc;
    private String release;
    private String criticality;
    private String shortDescription;
    private String modules;
    private String jiraProjectKey;
    private String lastUpdated;
    @Transient
    private String type;
    @Transient
    private String fetchType;
}

