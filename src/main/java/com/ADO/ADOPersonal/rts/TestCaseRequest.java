package com.ADO.ADOPersonal.rts;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TestCaseRequest {
    private String title;
    private String description;
    private String steps;
    private String state;
}
