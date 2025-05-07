package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "adoUrls")
public class AdoUrlConfig {
    @Id
    private String id;
    private String projectId;
    private String jiraProjectKey;
    private String adoApiUrl;
    private String adoApiIterationsUrl;
    private String adoApiToken;


}