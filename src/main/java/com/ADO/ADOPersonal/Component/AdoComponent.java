package com.ADO.ADOPersonal.Component;


import com.ADO.ADOPersonal.Client.AdoClient;
import com.ADO.ADOPersonal.Service.AdoService;
import com.ADO.ADOPersonal.Service.SchedulerInfoService;
import com.ADO.ADOPersonal.util.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Component
public class AdoComponent {

    @Autowired
    private AdoClient adoClient;

    @Autowired
    private SchedulerInfoService schedulerInfoService;

    @Autowired
    private AdoService adoService;

    @Value("${rbt.python.host}")
    private String pythonHost;


    @Async
    public CompletableFuture<Void> getIssuesAsync(String toolName, String projectId, String jiraProjectKey, String token) {


        String lastUpdatedDate = adoService.getMaxUpdatedDate(toolName, jiraProjectKey);

        String currentTimeStamp = getISTFormatDate(new Date());
        try {


            schedulerInfoService.save(toolName, jiraProjectKey, projectId, currentTimeStamp, "In-Progress");
            adoService.fetchAndSaveReleases(lastUpdatedDate, currentTimeStamp,jiraProjectKey,token);
            adoService.fetchAndSaveUserStories(lastUpdatedDate, currentTimeStamp,jiraProjectKey,token);
            adoService.fetchAndSaveDefects(lastUpdatedDate, currentTimeStamp,jiraProjectKey, token);
            adoService.fetchAndSaveTestCases(lastUpdatedDate, currentTimeStamp,jiraProjectKey, token);
            adoService.fetchAndSaveEpics(lastUpdatedDate, currentTimeStamp, jiraProjectKey, token);
            adoService.fetchAndSaveFeatures(lastUpdatedDate, currentTimeStamp, jiraProjectKey, token);
            System.out.println("Ingested data in GraphDB");

            schedulerInfoService.save(toolName, jiraProjectKey, projectId, currentTimeStamp, "Completed");

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            schedulerInfoService.save(toolName, jiraProjectKey, projectId, currentTimeStamp, "Failed");
            return CompletableFuture.completedFuture(null);
        }
    }

    public static String getISTFormatDate(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Instant utcInstant = date.toInstant();
//        ZonedDateTime zonedDateTime = utcInstant.atZone(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime zonedDateTime = utcInstant.atZone(ZoneId.of("UTC"));
        String formattedDate = formatter.format(zonedDateTime);
        return formattedDate;
    }

    private JSONObject prepareInputObjectForIngestion(String lastUpdatedDate, String jiraProjectKey) {

        JSONObject graphDbIngestObj = new JSONObject();
        graphDbIngestObj.put("project_name", jiraProjectKey);

        JSONArray ingestDataInGraphList = new JSONArray();

        JSONObject jsonObject = new JSONObject();


        jsonObject.put("collectionname", "userstories");
        jsonObject.put("jiraProjectKey", jiraProjectKey);
        jsonObject.put("field", "lastUpdated");
        jsonObject.put("value", lastUpdatedDate);
        jsonObject.put("type", "userstory");
        ingestDataInGraphList.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("collectionname", "testcase");
        jsonObject.put("jiraProjectKey", jiraProjectKey);
        jsonObject.put("field", "lastUpdated");
        jsonObject.put("value", lastUpdatedDate);
        jsonObject.put("type", "testcase");
        ingestDataInGraphList.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("collectionname", "defect");
        jsonObject.put("jiraProjectKey", jiraProjectKey);
        jsonObject.put("field", "lastUpdated");
        jsonObject.put("value", lastUpdatedDate);
        jsonObject.put("type", "defect");
        ingestDataInGraphList.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("collectionname", "test_execution_history");
        jsonObject.put("field", "lastUpdated");
        jsonObject.put("jiraProjectKey", jiraProjectKey);
        jsonObject.put("value", lastUpdatedDate);
        jsonObject.put("type", "testruns");
        ingestDataInGraphList.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("collectionname", "ReleaseTracker");
        jsonObject.put("field", "lastUpdated");
        jsonObject.put("jiraProjectKey", jiraProjectKey);
        jsonObject.put("value", lastUpdatedDate);
        jsonObject.put("type", "release");
        ingestDataInGraphList.add(jsonObject);

        graphDbIngestObj.put("data", ingestDataInGraphList);

        System.out.println("ingestDataInGraphList -- " + graphDbIngestObj);
        return graphDbIngestObj;
    }

    private void ingestDataInGraphDB(String token, JSONObject ingestDataInGraphObj) {

        try {
            String url = pythonHost + "/updategraphdb";

            StringBuilder response = new StringBuilder(Util.openConnectionForPostRequestGraphDB(url, token, ingestDataInGraphObj));
            System.out.println("response -- " + response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
