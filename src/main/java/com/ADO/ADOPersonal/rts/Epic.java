package com.ADO.ADOPersonal.rts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "epic")
public class Epic {
    @Id
    private String _id;
    private String epicId;
    private String title;
    private String lastUpdated;
}
