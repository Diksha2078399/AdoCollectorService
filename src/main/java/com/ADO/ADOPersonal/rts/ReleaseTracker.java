package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "ReleaseTracker")
public class ReleaseTracker {
    @Id
    private String _id;
    private String releaseName;
    private String projectid;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startDateView;
    private String endDateView;

    private List<Userstories> userstoryLst;

    private String userstoryCnt;
    private String graphLabel;
    private String jiraProjectKey;
    private String lastUpdated;

    @Transient
    private String type;
}

