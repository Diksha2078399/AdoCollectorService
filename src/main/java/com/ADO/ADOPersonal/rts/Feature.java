package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "feature")
public class Feature {
    @Id
    private String _id;
    private String featureId;
    private String title;
    private String lastUpdated;
}
