package com.ADO.ADOPersonal.Service;


import com.ADO.ADOPersonal.Client.AdoClient;
import com.ADO.ADOPersonal.db.repo.*;
import com.ADO.ADOPersonal.metadata.SchedulerInfo;


import com.ADO.ADOPersonal.rts.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service
public class AdoService {

    @Autowired
    private MongoTemplate primaryMongoTemplate;



    @Autowired
    private AdoClient adoClient;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private RtsTestcasesRepo testCaseRepository;

    @Autowired
    private TestCaseExecutionHistoryRepo testRunRepository;
    @Autowired
    private SchedulerInfoRepository schedulerInfoServiceRepo;


    @Autowired
    private EpicRepository epicRepository;
    @Autowired
    private FeatureRepository featureRepository;


    public void updateUserStory(String id,String projectId,String jiraProjectKey,String token, Map<String, Object> updateFields) throws Exception {
        adoClient.updateUserStory(id,projectId,jiraProjectKey,token,updateFields);
    }

    public String createTestCase(TestCaseRequest request, String jiraProjectKey, String token) throws Exception {
        return adoClient.createTestCase(request,jiraProjectKey,token);
    }

    public String updateTestCase(String id, TestCaseRequest request, String jiraProjectKey, String token) throws Exception {
        return adoClient.updateTestCase(id, request, jiraProjectKey, token);
    }

    public String updateDefect(String id, Defect request, String jiraProjectKey, String token) throws Exception {
        return adoClient.updateDefect(id, request, jiraProjectKey, token);
    }


    public String fetchUniqueWorkItemTypes(String jiraProjectKey, String token) throws Exception {
        return adoClient.fetchUniqueWorkItemTypes(jiraProjectKey, token);
    }
    public Map<String, String> fetchUsers(String jiraProjectKey, String token) throws Exception {
        return adoClient.fetchUsers(jiraProjectKey, token);
    }


    public List<String> fetchWorkItemTypes(String jiraProjectKey, String token) throws Exception {
        return adoClient.fetchWorkItemTypes(jiraProjectKey, token);
    }

    public List<byte[]> getDefectImages(String id, String jiraProjectKey, String token) throws Exception {
        return adoClient.getDefectImages(id, jiraProjectKey, token);
    }
    public String getMaxUpdatedDate(String toolName, String projectKey) {

        SchedulerInfo schedulerInfo = schedulerInfoServiceRepo.findByToolNameAndJiraProjectKey(toolName, projectKey);
        System.out.println("scedulerInfo -- *** " + schedulerInfo);
        return schedulerInfo == null ? null : schedulerInfo.getLastUpdatedDate();
    }

    public void fetchAndSaveUserStories(String lastUpdatedDate, String currentTimeStamp, String jiraProjectKey,String token) {
        List<Userstories> userStories = (List<Userstories>) adoClient.fetchWorkItemsFromADO("User Story", lastUpdatedDate, Userstories.class,jiraProjectKey,token);
        System.out.println("userStories size: " + userStories.size());
        for (Userstories userStory : userStories) {
            Query query = new Query(Criteria.where("userStoryID").is(userStory.getUserStoryID()));
            Userstories existingUserStory = primaryMongoTemplate.findOne(query, Userstories.class);

            if (existingUserStory != null) {
                userStory.set_id(existingUserStory.get_id());
            }
            userStory.setLastUpdated(currentTimeStamp);

            primaryMongoTemplate.save(userStory);
        }
    }

    public void fetchAndSaveReleases(String lastUpdatedDate, String currentTimeStamp,String jiraProjectKey, String token) {
        List<ReleaseTracker> releaseTrackers = adoClient.fetchIterationsFromAdo(currentTimeStamp,jiraProjectKey,token);
        for (ReleaseTracker releaseTracker : releaseTrackers) {
            primaryMongoTemplate.save(releaseTracker);
        }
    }



    public void fetchAndSaveDefects(String lastUpdatedDate, String currentTimeStamp,String jiraProjectKey,String token) {
        List<Defect> defects = (List<Defect>) adoClient.fetchWorkItemsFromADO("Bug", lastUpdatedDate, Defect.class,jiraProjectKey,token);
        System.out.println("Defects size: " + defects.size());
        for (Defect defect : defects) {
            Query query = new Query(Criteria.where("defid").is(defect.getDefid()));
            Defect existingDefect = primaryMongoTemplate.findOne(query, Defect.class);

            if (existingDefect != null) {
                defect.setId(existingDefect.getId());
            }
            defect.setLastUpdated(currentTimeStamp);
            primaryMongoTemplate.save(defect);
        }
    }

    public void fetchAndSaveTestCases(String lastUpdatedDate, String currentTimeStamp,String jiraProjectKey,String token) {
        List<RTSTestCase> testCases = (List<RTSTestCase>) adoClient.fetchWorkItemsFromADO("Test Case", lastUpdatedDate, RTSTestCase.class,jiraProjectKey,token);
        System.out.println("TestCases size: " + testCases.size());
        for (RTSTestCase testCase : testCases) {
            Query query = new Query(Criteria.where("testCaseID").is(testCase.getTestCaseID()));
            RTSTestCase existingTestCase = primaryMongoTemplate.findOne(query, RTSTestCase.class);

            if (existingTestCase != null) {
                testCase.set_id(existingTestCase.get_id());
            }
            testCase.setLastUpdated(currentTimeStamp);
            primaryMongoTemplate.save(testCase);
        }
    }

    public void fetchAndSaveEpics(String lastUpdatedDate, String currentTimeStamp, String jiraProjectKey, String token) {
        List<Epic> epics = (List<Epic>) adoClient.fetchWorkItemsFromADO("Epic", lastUpdatedDate, Epic.class, jiraProjectKey, token);
        System.out.println("Epics size: " + epics.size());
        for (Epic epic : epics) {
            Query query = new Query(Criteria.where("id").is(epic.getEpicId()));
            Epic existingEpic = primaryMongoTemplate.findOne(query, Epic.class);
            if (existingEpic != null) {
                epic.set_id(existingEpic.get_id());
            }
            epic.setLastUpdated(currentTimeStamp);
            primaryMongoTemplate.save(epic);
        }
    }

    public void fetchAndSaveFeatures(String lastUpdatedDate, String currentTimeStamp, String jiraProjectKey, String token) {
        List<Feature> features = (List<Feature>) adoClient.fetchWorkItemsFromADO("Feature", lastUpdatedDate, Feature.class, jiraProjectKey, token);
        System.out.println("Features size: " + features.size());
        for (Feature feature : features) {
            Query query = new Query(Criteria.where("id").is(feature.getFeatureId()));
            Feature existingFeature = primaryMongoTemplate.findOne(query, Feature.class);
            if (existingFeature != null) {
                feature.set_id(existingFeature.get_id());
            }
            feature.setLastUpdated(currentTimeStamp);
            primaryMongoTemplate.save(feature);
        }
    }


    public void fetchAndSaveTestRuns(String lastUpdatedDate, String currentTimeStamp,String jiraProjectKey, String token) {
        List<TestExecutionHistory> testRuns = adoClient.fetchTestRunsFromAdo(lastUpdatedDate,jiraProjectKey, token);
        for (TestExecutionHistory testRun : testRuns) {
            Query query = new Query(Criteria.where("testCaseId").is(testRun.getTestCaseId()));
            TestExecutionHistory existingTestRun = primaryMongoTemplate.findOne(query, TestExecutionHistory.class);

            if (existingTestRun != null) {
                // Compare the System.ChangedDate from ADO with the lastUpdated date in MongoDB
                if (testRun.getLastUpdated().compareTo(existingTestRun.getLastUpdated()) > 0) {
                    testRun.setId(existingTestRun.getId());
                    testRun.setLastUpdated(currentTimeStamp);
                    primaryMongoTemplate.save(testRun);
                }
            } else {
                testRun.setLastUpdated(currentTimeStamp);
                primaryMongoTemplate.save(testRun);
            }
        }
    }

    @NotNull
    public static String getUTCFormattedDate(String lastUpdatedDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        LocalDateTime dateTime = LocalDateTime.parse(lastUpdatedDate, inputFormatter);
        String formattedDate = dateTime.format(outputFormatter);

        System.out.println(formattedDate);

        lastUpdatedDate = formattedDate;
        return lastUpdatedDate;
    }

}
