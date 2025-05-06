package com.ADO.ADOPersonal.Client;


import com.ADO.ADOPersonal.Service.AdoService;
import com.ADO.ADOPersonal.db.repo.ReleaseRepository;
import com.ADO.ADOPersonal.rts.*;
import com.ADO.ADOPersonal.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.json.simple.JSONObject;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AdoClient {


    @Value("${spring.datasource.ado.url}")
    private String dataSourceUrl;

    @Autowired
    private ReleaseRepository releaseTrackerRepository;


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private AdoService adoService;

    private String adoApiBaseUrl;
    private String adoApiUrl;
    private String adoWorkItemsUrl;

    private String adoApiToken;
    private String adoApiIterationUrl;


    public void setAdoApiUrl(String adoApiUrl) {
        this.adoApiBaseUrl = adoApiUrl;
    }




    public void setAdoApiToken(String adoApiToken) {
        this.adoApiToken = adoApiToken;
    }

    public void setAdoApiIterationUrl(String adoApiIterationUrl) {
        this.adoApiIterationUrl = adoApiIterationUrl;
    }



    private String fetchDataSourceConfig(String token) throws Exception {
        StringBuffer response = Util.openConnection(dataSourceUrl, token);
        return response.toString();
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }



    private void initializeUrlsFromDataSource(String jiraProjectKey, String token) throws Exception {
        String response = fetchDataSourceConfig(token);
        JSONObject jsonResponse = new JSONObject(response);

        String baseUrl = jsonResponse.getString("url");
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        this.adoApiBaseUrl = baseUrl;
        System.out.println(adoApiBaseUrl);
        this.adoApiToken = jsonResponse.getString("password");
//        this.adoApiIterationUrl = "https://dev.azure.com/NeuroAITeam/OracleHCM/OracleHCM Team";
        this.adoApiUrl = adoApiBaseUrl + "/_apis/wit/wiql?$expand=relations&timePrecision=true&api-version=7.1";
        this.adoWorkItemsUrl = adoApiBaseUrl + "/_apis/wit/workitems?ids={ids}&$expand=relations&api-version=7.1";
        this.adoApiIterationUrl = adoApiBaseUrl + jiraProjectKey + " Team";
        this.adoApiIterationUrl = this.adoApiIterationUrl.contains(" ") ? this.adoApiIterationUrl.replaceAll(" ", "%20") : this.adoApiIterationUrl;
        System.out.println(adoApiIterationUrl);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encodedToken = Base64.getEncoder().encodeToString((":" + adoApiToken).getBytes());
        headers.set("Authorization", "Basic " + encodedToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public List<ReleaseTracker> fetchIterationsFromAdo(String currentTimeStamp,String jiraProjectKey, String token) {
        try {
            initializeUrlsFromDataSource(jiraProjectKey, token);
            String url = adoApiBaseUrl +"/_apis/work/teamsettings/iterations?api-version=7.1";
            //String url = adoApiIterationUrl + "/_apis/work/teamsettings/iterations?api-version=7.1";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
//            String encodedUrl = encodeUrl(url);
//            System.out.println("Encoded URL: " + encodedUrl);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            Map<String, Object> iterationsResult = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> iterations = (List<Map<String, Object>>) iterationsResult.get("value");

            Set<ReleaseTracker> releaseTrackers = new HashSet<>();
            DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;


            // Extract the LocalDate part

            for (Map<String, Object> iteration : iterations) {
                ReleaseTracker releaseTracker = new ReleaseTracker();
                releaseTracker.setReleaseName((String) iteration.getOrDefault("name", ""));
                Map<String, Object> attributes = (Map<String, Object>) iteration.get("attributes");

                if (attributes != null) {
                    String startDateStr = (String) attributes.get("startDate");
//                    DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

                    ZonedDateTime startDateTime = ZonedDateTime.parse(startDateStr, formatter);

                    String endDateStr = (String) attributes.get("finishDate");
                    ZonedDateTime endDateTime = ZonedDateTime.parse(endDateStr, formatter);
                    LocalDate endLocalDate = endDateTime.toLocalDate();
                    releaseTracker.setEndDate(endLocalDate);
                } else {
                    releaseTracker.setStartDate(null);
                    releaseTracker.setEndDate(null);
                }

                String path = (String) iteration.get("path");
                System.out.println("path - " + path);
                String adoProjectKey = path != null ? path.split("\\\\")[0] : "";

                releaseTracker.setJiraProjectKey(adoProjectKey);

                releaseTracker.setLastUpdated(currentTimeStamp);

                ReleaseTracker existingReleaseTracker = releaseTrackerRepository.findByReleaseName(releaseTracker.getReleaseName());
                if (existingReleaseTracker != null) {
                    releaseTracker.set_id(existingReleaseTracker.get_id());
                }

                releaseTrackers.add(releaseTracker);
            }

            releaseTrackerRepository.saveAll(releaseTrackers);

            return new ArrayList<>(releaseTrackers);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching iterations from ADO: " + e.getMessage());
            return Collections.emptyList();
        }
    }




    public List<?> fetchWorkItemsFromADO(String workItemType, String lastUpdatedDate, Class<?> className, String jiraProjectKey, String token) {
        try {

            initializeUrlsFromDataSource(jiraProjectKey, token);
            String query;

            if (lastUpdatedDate != null) {
                lastUpdatedDate = AdoService.getUTCFormattedDate(lastUpdatedDate);
                System.out.println("Last Updated Date: " + lastUpdatedDate);
                query = String.format(
                        "SELECT * FROM WorkItems WHERE [System.WorkItemType] = '%s'" +
                                "AND ([System.CreatedDate] >= '%s' OR [System.ChangedDate] >= '%s')",
                        workItemType, lastUpdatedDate, lastUpdatedDate
                );
            } else {
                System.out.println("in else as lastupdated is null");
                query = String.format("SELECT * FROM WorkItems WHERE [System.WorkItemType] = '%s'", workItemType);
            }
            System.out.println("query --- " + query);
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("query", query);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonQuery = objectMapper.writeValueAsString(queryMap);

            HttpEntity<String> queryEntity = new HttpEntity<>(jsonQuery, createHeaders());
            ResponseEntity<String> queryResponse = restTemplate.exchange(adoApiUrl, HttpMethod.POST, queryEntity, String.class);

            return parseWorkItems(queryResponse.getBody(), className, jiraProjectKey,  token);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching user stories from ADO: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    private <T> List<T> parseWorkItems(String responseBody, Class<T> clazz,String jiraProjectKey, String token) {
        try {
            Map<String, Object> queryResult = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> workItems = (List<Map<String, Object>>) queryResult.get("workItems");
            if (workItems != null && !workItems.isEmpty()) {
                List<T> allItems = new ArrayList<>();
                int batchSize = 100;
                for (int i = 0; i < workItems.size(); i += batchSize) {
                    StringBuilder ids = new StringBuilder();
                    for (int j = i; j < i + batchSize && j < workItems.size(); j++) {
                        ids.append(workItems.get(j).get("id")).append(",");
                    }
                    if (ids.length() > 0) {
                        ids.setLength(ids.length() - 1);
                        String workItemsUrl = adoWorkItemsUrl.replace("{ids}", ids.toString());
                        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
                        ResponseEntity<String> response = restTemplate.exchange(workItemsUrl, HttpMethod.GET, entity, String.class);
                        Map<String, Object> workItemsResult = objectMapper.readValue(response.getBody(), Map.class);
                        List<Map<String, Object>> workItemsDetails = (List<Map<String, Object>>) workItemsResult.get("value");
                        for (Map<String, Object> workItemDetail : workItemsDetails) {
                            T item = mapToEntity(workItemDetail, clazz,jiraProjectKey, token);
                            allItems.add(item);
                        }
                    }
                }
                return allItems;
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error parsing work items: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<TestExecutionHistory> fetchTestRunsFromAdo(String lastUpdatedDate, String jiraProjectKey, String token) {
        String query = "{\"query\": \"Select [System.Id] From WorkItems Where [System.WorkItemType] = 'Test Run'\"}";

        HttpEntity<String> queryEntity = new HttpEntity<>(query, createHeaders());
        ResponseEntity<String> queryResponse = restTemplate.exchange(adoApiUrl, HttpMethod.POST, queryEntity, String.class);
        return parseWorkItems(queryResponse.getBody(), TestExecutionHistory.class, jiraProjectKey,  token);
    }


    private String convertPriority(Object priority) {
        if (priority == null) {
            return "";
        }
        switch (priority.toString()) {
            case "1":
                return "Critical";
            case "2":
                return "High";
            case "3":
                return "Medium";
            case "4":
                return "Low";
            default:
                return "";
        }
    }

    private <T> T mapToEntity(Map<String, Object> workItemDetail, Class<T> clazz,String jiraProjectKey, String token) {
        T entity = null;
        try {
            Map<String, Object> fields = (Map<String, Object>) workItemDetail.get("fields");


            if (clazz == Userstories.class) {
                Userstories userStory = new Userstories();
                userStory.setUserStoryID(workItemDetail.get("id").toString());
                userStory.setDescription(fields.get("System.Description") != null ? convertHtmlToString(fields.get("System.Description").toString()) : "");
                userStory.setCreatedOn(fields.get("System.CreatedDate") != null ? convertToDate(fields.get("System.CreatedDate").toString()) : null);
                userStory.setSprint(fields.get("System.IterationPath") != null ? fields.get("System.IterationPath").toString() : "");
                userStory.setTeam(fields.get("System.TeamProject") != null ? fields.get("System.TeamProject").toString() : "");
                userStory.setAuthor(fields.get("System.CreatedBy") != null ? extractDisplayName(fields.get("System.CreatedBy").toString()) : "");
                userStory.setOwner(fields.get("System.AssignedTo") != null ? extractDisplayName(fields.get("System.AssignedTo").toString()) : "");
                userStory.setRelease(fields.get("Custom.Release") != null ? fields.get("Custom.Release").toString() : "");
                // userStory.setCriticality(fields.get("Microsoft.VSTS.Common.Priority") != null ? fields.get("Microsoft.VSTS.Common.Priority").toString() : "");
                String severity = fields.get("Microsoft.VSTS.Common.Severity") != null ? fields.get("Microsoft.VSTS.Common.Severity").toString() : "";
                if (!severity.isEmpty() && severity.contains(" - ")) {
                    severity = severity.split(" - ")[1];
                }
                userStory.setCriticality(severity);

                userStory.setShortDescription(fields.get("System.Title") != null ? convertHtmlToString(fields.get("System.Title").toString()) : "");
                userStory.setModules(fields.get("Custom.Module") != null ? fields.get("Custom.Module").toString() : "");
                System.out.println(fields.get("Custom.Module"));
                userStory.setJiraProjectKey(fields.get("System.TeamProject") != null ? fields.get("System.TeamProject").toString() : "");
                userStory.setSimilarityPerc(fields.get("Custom.SimilarityPerc") != null ? fields.get("Custom.SimilarityPerc").toString() : "");

                entity = clazz.cast(userStory);


            } else if (clazz == Defect.class) {
                Defect defect = new Defect();
                System.out.println("Defect Fields:");
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    System.out.println("**************************"+entry.getKey() + ": " + entry.getValue());
                }
                defect.setDefid(workItemDetail.get("id").toString());
                System.out.println(workItemDetail.get("id").toString());
                defect.setDescription(fields.get("System.Description") != null ? convertHtmlToString(fields.get("System.Description").toString()): "");
                defect.setDetectedBY(fields.get("System.CreatedBy") != null ? extractDisplayName(fields.get("System.CreatedBy")) : "");
                defect.setDetectedDate(fields.get("System.CreatedDate") != null ? fields.get("System.CreatedDate").toString() : "");

                defect.setLinkedTestCases(extractLinkedTestCases((List<Map<String, Object>>) workItemDetail.get("relations"),jiraProjectKey,token));
                defect.setLinkedUserstories(extractLinkedUserStories((List<Map<String, Object>>) workItemDetail.get("relations"),jiraProjectKey, token));
                defect.setModule(fields.get("Custom.Module") != null ? fields.get("Custom.Module").toString() : "");
                System.out.println(fields.get("Custom.Module"));

                defect.setPriority(convertPriority(fields.get("Microsoft.VSTS.Common.Priority") != null ? fields.get("Microsoft.VSTS.Common.Priority").toString() : ""));

                defect.setRelease(fields.get("Custom.Release") != null ? fields.get("Custom.Release").toString() : "");
                String severity = fields.get("Microsoft.VSTS.Common.Severity") != null ? fields.get("Microsoft.VSTS.Common.Severity").toString() : "";
                if (!severity.isEmpty() && severity.contains(" - ")) {
                    severity = severity.split(" - ")[1];
                }
                defect.setSeverity(severity);
                defect.setReproSteps(fields.get("Microsoft.VSTS.TCM.ReproSteps") != null ? convertHtmlToString(fields.get("Microsoft.VSTS.TCM.ReproSteps").toString()) : "");
                defect.setActualRootcause(fields.get("Custom.ActualRootcause") != null ? fields.get("Custom.ActualRootcause").toString() : "");
                //defect.setAssignedTo(fields.get("System.AssignedTo") != null ? fields.get("System.AssignedTo").toString() : "");
                defect.setAssignedTo(fields.get("System.AssignedTo") != null ? extractDisplayName(fields.get("System.AssignedTo")) : "");
                System.out.println("***************System.AssignedTo"+ fields.get("System.AssignedTo"));
                defect.setClosedDate(fields.get("Microsoft.VSTS.Common.ClosedDate") != null ? fields.get("Microsoft.VSTS.Common.ClosedDate").toString() : "");
                defect.setResolutionNotes(fields.get("Microsoft.VSTS.Common.Resolution") != null ? fields.get("Microsoft.VSTS.Common.Resolution").toString() : "");
                defect.setShortDescription(fields.get("System.Title") != null ? convertHtmlToString(fields.get("System.Title").toString()) : "");
                defect.setSprint(fields.get("System.IterationPath") != null ? fields.get("System.IterationPath").toString() : "");
                defect.setStatus(fields.get("System.State") != null ? fields.get("System.State").toString() : "");
                defect.setJiraProjectKey(fields.get("System.TeamProject") != null ? fields.get("System.TeamProject").toString() : "");



                // Check if the defect contains images in attachments
                List<String> imagePaths = extractImagePathsFromAttachments((List<Map<String, Object>>) workItemDetail.get("relations"));
                defect.setImagePaths(imagePaths);
                defect.setImageContain(!imagePaths.isEmpty());

                entity = clazz.cast(defect);
            } else if (clazz == RTSTestCase.class) {

                RTSTestCase testCase = new RTSTestCase();
                System.out.println(fields.get("System.ChangedDate") + "**************************************************");
                testCase.setTestCaseID(workItemDetail.get("id").toString());
                testCase.setDescription(fields.get("System.Description") != null ? convertHtmlToString(fields.get("System.Description").toString()) : "");
                testCase.setModule(fields.get("Custom.Module") != null ? fields.get("Custom.Module").toString() : "");
                testCase.setPriority(convertPriority(fields.get("Microsoft.VSTS.Common.Priority") != null ? fields.get("Microsoft.VSTS.Common.Priority").toString() : ""));
                testCase.setCreatedOn(fields.get("System.CreatedDate") != null ? fields.get("System.CreatedDate").toString() : "");

                testCase.setLinkedUserstories(extractLinkedUserStories((List<Map<String, Object>>) workItemDetail.get("relations"),jiraProjectKey,token));
                testCase.setRelease(fields.get("Custom.Release") != null ? fields.get("Custom.Release").toString() : "");
                testCase.setRelease(fields.get("Custom.Release") != null ? fields.get("Custom.Release").toString() : "");
                testCase.setTestCaseName(fields.get("System.Title") != null ? fields.get("System.Title").toString() : "");
                testCase.setType(fields.get("System.WorkItemType") != null ? fields.get("System.WorkItemType").toString() : "");
                testCase.setProjectid(fields.get("System.TeamProject") != null ? fields.get("System.TeamProject").toString() : "");
                testCase.setScriptName(fields.get("Custom.ScriptName") != null ? fields.get("Custom.ScriptName").toString() : "");
                testCase.setTestCaseType(fields.get("Custom.TestCaseType") != null ? fields.get("Custom.TestCaseType").toString() : "");
                testCase.setSprint(fields.get("System.IterationPath") != null ? fields.get("System.IterationPath").toString() : "");

                String stepsXml = fields.get("Microsoft.VSTS.TCM.Steps") != null ? fields.get("Microsoft.VSTS.TCM.Steps").toString() : "";

                List<String> testSteps = parseActions(stepsXml).stream().map(this::convertHtmlToString).collect(Collectors.toList());
                testCase.setTestStep(testSteps);
                List<String> expectedResults = parseExpectedResults(stepsXml).stream().map(this::convertHtmlToString).collect(Collectors.toList());
                testCase.setExpectedResult(expectedResults);

                testCase.setJiraProjectKey(fields.get("System.TeamProject") != null ? fields.get("System.TeamProject").toString() : "");
                testCase.setBusinessCritical(fields.get("Microsoft.VSTS.Common.BusinessCritical") != null ? Boolean.parseBoolean(fields.get("Microsoft.VSTS.Common.BusinessCritical").toString()) : false);
                entity = clazz.cast(testCase);

            } else if (clazz == TestExecutionHistory.class) {
                TestExecutionHistory testRun = new TestExecutionHistory();
                testRun.setId(workItemDetail.get("id").toString());
                testRun.setTestCaseId(workItemDetail.get("testCaseId") != null ? workItemDetail.get("testCaseId").toString() : "");
                testRun.setJiraProjectKey(workItemDetail.get("ADOProjectKey") != null ? workItemDetail.get("ADOProjectKey").toString() : "");
                testRun.setTestSuiteId(workItemDetail.get("testSuiteId") != null ? workItemDetail.get("testSuiteId").toString() : "");
                testRun.setTestSuiteName(workItemDetail.get("testSuiteName") != null ? workItemDetail.get("testSuiteName").toString() : "");
                testRun.setTestcaseExecutionId(workItemDetail.get("testcaseExecutionId") != null ? workItemDetail.get("testcaseExecutionId").toString() : "");
                testRun.setTestExecutionStatus(workItemDetail.get("state") != null ? workItemDetail.get("state").toString() : "");

                entity = clazz.cast(testRun);
            }else if (clazz == Epic.class) {
                Epic epic = new Epic();
                epic.setEpicId(workItemDetail.get("id").toString());
                epic.setTitle(fields.get("System.Title") != null ? fields.get("System.Title").toString() : "");
                entity = clazz.cast(epic);
            } else if (clazz == Feature.class) {
                Feature feature = new Feature();
                feature.setFeatureId(workItemDetail.get("id").toString());
                feature.setTitle(fields.get("System.Title") != null ? fields.get("System.Title").toString() : "");
                entity = clazz.cast(feature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    public String convertHtmlToString(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }
        return Jsoup.parse(htmlContent).text();
    }


    private String extractDisplayName(Object field) {
        if (field == null) {
            return "";
        }
        String fieldValue = field.toString();
        // Find the start and end of the displayName value
        int startIndex = fieldValue.indexOf("displayName=") + "displayName=".length();
        int endIndex = fieldValue.indexOf(", url=");
        if (startIndex != -1 && endIndex != -1) {
            return fieldValue.substring(startIndex, endIndex);
        }
        return fieldValue;
    }
    //image in system .info
    private List<String> extractImagePaths(String systemInfo) {
        List<String> imagePaths = new ArrayList<>();
        if (systemInfo != null && !systemInfo.isEmpty()) {
            org.jsoup.nodes.Document doc = Jsoup.parse(systemInfo);
            org.jsoup.select.Elements imgElements = doc.select("img");
            for (org.jsoup.nodes.Element img : imgElements) {
                imagePaths.add(img.attr("src"));
            }
        }
        return imagePaths;
    }

    private List<String> extractImagePathsFromAttachments(List<Map<String, Object>> relations) {
        List<String> imagePaths = new ArrayList<>();
        if (relations != null) {
            for (Map<String, Object> relation : relations) {
                if ("AttachedFile".equals(relation.get("rel"))) {
                    Map<String, Object> attributes = (Map<String, Object>) relation.get("attributes");
                    String name = (String) attributes.get("name");
                    if (name != null && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif"))) {
                        imagePaths.add((String) relation.get("url"));
                    }
                }
            }
        }
        return imagePaths;
    }

    public String getWorkItemType(String linkedIdOfParent,String jiraProjectKey, String token) throws Exception {

        initializeUrlsFromDataSource(jiraProjectKey, token);


        String url = adoApiBaseUrl + "_apis/wit/workItems/" + linkedIdOfParent + "?api-version=7.1";
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+url);
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Parse the response to extract the work item type
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode fieldsNode = rootNode.path("fields");
        String workItemType = fieldsNode.path("System.WorkItemType").asText();

        return workItemType;
    }
    private String extractLinkedUserStories(List<Map<String, Object>> relations,String jiraProjectKey, String token) {
        if (relations == null) {
            return "";
        }
        List<String> linkedUserStories = new ArrayList<>();
        for (Map<String, Object> relation : relations) {
            if ("System.LinkTypes.Hierarchy-Reverse".equals(relation.get("rel"))) {
                String url = (String) relation.get("url");
                String[] parts = url.split("/");
                String id = parts[parts.length - 1];
                try {
                    String workItemType = getWorkItemType(id,jiraProjectKey,token);
                    System.out.println("parent_id::::::::::::::::"+id);
                    if ("User Story".equals(workItemType)) {
                        linkedUserStories.add(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return String.join(",", linkedUserStories);
    }

    private String extractLinkedTestCases(List<Map<String, Object>> relations,String jiraProjectKey, String token) {
        if (relations == null) {
            return "";
        }
        List<String> linkedTestCases = new ArrayList<>();
        for (Map<String, Object> relation : relations) {
            if ("System.LinkTypes.Hierarchy-Reverse".equals(relation.get("rel"))) {
                String url = (String) relation.get("url");
                String[] parts = url.split("/");
                String id = parts[parts.length - 1];
                try {
                    String workItemType = getWorkItemType(id,jiraProjectKey,token);
                    if ("Test Case".equals(workItemType)) {
                        linkedTestCases.add(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return String.join(",", linkedTestCases);
    }


    private List<String> parseActions(String stepsXml) {
        List<String> actions = new ArrayList<>();
        if (stepsXml != null && !stepsXml.isEmpty()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(stepsXml)));
                NodeList stepNodes = document.getElementsByTagName("step");
                for (int i = 0; i < stepNodes.getLength(); i++) {
                    Element stepElement = (Element) stepNodes.item(i);
                    NodeList parameterizedStringNodes = stepElement.getElementsByTagName("parameterizedString");
                    if (parameterizedStringNodes.getLength() > 0) {
                        String actionText = parameterizedStringNodes.item(0).getTextContent();
                        actions.add(actionText);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return actions;
    }

    private List<String> parseExpectedResults(String stepsXml) {
        List<String> expectedResults = new ArrayList<>();
        if (stepsXml != null && !stepsXml.isEmpty()) {
            try {
                System.out.println("@@@@@@@@@@ - " + stepsXml);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(stepsXml)));
                NodeList stepNodes = document.getElementsByTagName("step");
                for (int i = 0; i < stepNodes.getLength(); i++) {
                    Element stepElement = (Element) stepNodes.item(i);
                    NodeList parameterizedStringNodes = stepElement.getElementsByTagName("parameterizedString");
                    if (parameterizedStringNodes.getLength() > 1) {
                        String expectedResultText = parameterizedStringNodes.item(1).getTextContent();
                        expectedResults.add(expectedResultText);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return expectedResults;
    }

    private Date convertToDate(String dateString){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {
            Date date = formatter.parse(dateString);
            System.out.println(date);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateUserStory(String id, String projectId,String jiraProjectKey,String token, Map<String, Object> updateFields) throws Exception {

        initializeUrlsFromDataSource(jiraProjectKey, token);
        String url = adoApiBaseUrl + "/_apis/wit/workitems/" + id + "?api-version=7.1";
        System.out.println("**********************Update User Story URL: " + url);
        List<Map<String, String>> patchDocument = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updateFields.entrySet()) {
            Map<String, String> patchOperation = new HashMap<>();
            patchOperation.put("op", "replace");
            patchOperation.put("path", "/fields/" + entry.getKey());
            patchOperation.put("value", entry.getValue().toString());
            patchDocument.add(patchOperation);
        }

        String jsonPatch = objectMapper.writeValueAsString(patchDocument);
        HttpEntity<String> entity = new HttpEntity<>(jsonPatch, createHeadersForPatch());
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
    public String updateDefect(String id, Defect request, String jiraProjectKey, String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        String url = adoApiBaseUrl + "_apis/wit/workitems/" + id + "?api-version=7.1";

        List<Map<String, Object>> requestBody = new ArrayList<>();
        requestBody.add(createPatchOperation("add", "/fields/Microsoft.VSTS.TCM.ReproSteps", request.getDescription()));
        requestBody.add(createPatchOperation("add", "/fields/System.Description", request.getDescription()));
        requestBody.add(createPatchOperation("add", "/fields/System.History", request.getComments().replace("\n", "<br>")));
        requestBody.add(createPatchOperation("add", "/fields/Microsoft.VSTS.TCM.SystemInfo", request.getSystemInfo()));
        requestBody.add(createPatchOperation("add", "/fields/System.AssignedTo", request.getAssignedTo()));

        try {
            String jsonPatch = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonPatch, createHeadersForPatch());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private HttpHeaders createHeadersForPatch() {
        HttpHeaders headers = new HttpHeaders();
        //   String token1= "BoenNWuMSEpAiTkQYe4UAuLOPo6gfChB860Lva0ETeEhsbFISgjaJQQJ99BAACAAAAA6DxLKAAAGAZDOCv5C";
        String encodedToken = Base64.getEncoder().encodeToString((":" + adoApiToken).getBytes());
        //String encodedToken = Base64.getEncoder().encodeToString((":" + token1).getBytes());
        headers.set("Authorization", "Basic " + encodedToken);
        headers.set("Content-Type", "application/json-patch+json");
        headers.set("X-HTTP-Method-Override", "PATCH");
        return headers;
    }



    public static final MediaType APPLICATION_JSON_PATCH_JSON = MediaType.valueOf("application/json-patch+json");

    public String createTestCase(TestCaseRequest request,String jiraProjectKey,String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        String url = adoApiBaseUrl+"_apis/wit/workitems/$Test Case?api-version=7.1";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON_PATCH_JSON);
        headers.setBearerAuth(adoApiToken);

        List<Map<String, Object>> requestBody = new ArrayList<>();
        requestBody.add(createPatchOperation("add", "/fields/System.Title", request.getTitle()));
        requestBody.add(createPatchOperation("add", "/fields/Microsoft.VSTS.TCM.Steps", request.getSteps()));
        requestBody.add(createPatchOperation("add", "/fields/System.State", request.getState()));

        try {
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

            System.out.println("Request URL: " + url);
            System.out.println("Request Headers: " + headers);
            System.out.println("Request Body: " + jsonRequestBody);

            String encodedUrl = encodeUrl(url);
            System.out.println("Encoded URL: " + encodedUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            System.out.println("Response Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String updateTestCase(String id, TestCaseRequest request, String jiraProjectKey, String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        String url = adoApiBaseUrl + "_apis/wit/workitems/" + id + "?api-version=7.1";


        List<Map<String, Object>> requestBody = new ArrayList<>();
        requestBody.add(createPatchOperation("add", "/fields/System.Title", request.getTitle()));
        requestBody.add(createPatchOperation("add", "/fields/Microsoft.VSTS.TCM.Steps", request.getSteps()));
        requestBody.add(createPatchOperation("add", "/fields/System.State", request.getState()));

        try {

            String jsonPatch = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonPatch, createHeadersForPatch());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> createPatchOperation(String op, String path, String value) {
        Map<String, Object> patchOperation = new HashMap<>();
        patchOperation.put("op", op);
        patchOperation.put("path", path);
        patchOperation.put("value", value);
        return patchOperation;
    }

    public List<byte[]> getDefectImages(String id, String jiraProjectKey, String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        String url = adoApiBaseUrl + "/_apis/wit/workitems/" + id + "?$expand=relations&api-version=7.1";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        Map<String, Object> workItemDetail = objectMapper.readValue(response.getBody(), Map.class);
        Map<String, Object> fields = (Map<String, Object>) workItemDetail.get("fields");

        // Check for images in System.Info
        String systemInfo = fields.get("Microsoft.VSTS.TCM.SystemInfo") != null ? fields.get("Microsoft.VSTS.TCM.SystemInfo").toString() : "";
        List<String> imagePaths = extractImagePaths(systemInfo);

        // Check for images in attachments
        imagePaths.addAll(extractImagePathsFromAttachments((List<Map<String, Object>>) workItemDetail.get("relations")));

        List<byte[]> imageBytesList = new ArrayList<>();
        for (String imageUrl : imagePaths) {
            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(imageUrl, HttpMethod.GET, entity, byte[].class);
            System.out.println("****************************"+imageResponse.getBody());

            imageBytesList.add(imageResponse.getBody());

        }

        return imageBytesList;
    }
    public Map<String, String> fetchUsers(String jiraProjectKey, String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        Map<String, String> orgAndProject = extractOrganizationAndProjectFromBaseUrl(adoApiBaseUrl);
        String organization = orgAndProject.get("organization");
        String url = "https://vsaex.dev.azure.com/" + organization + "/_apis/userentitlements?api-version=7.1-preview.1";
        System.out.println("####################"+url);
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
        return parseUserResponse(response.getBody());

    }

    private Map<String, String> extractOrganizationAndProjectFromBaseUrl(String baseUrl) {
        String[] parts = baseUrl.split("/");
        Map<String, String> result = new HashMap<>();
        result.put("organization", parts[3]);
        result.put("projectName", parts[4]);
        return result;
    }

    private Map<String, String> parseUserResponse(String responseBody) throws IOException {
        Map<String, String> userMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);

        // Print the entire response body for debugging
        System.out.println("Response Body: " + responseBody);

        JsonNode valueNode = rootNode.path("value");

        // Check if the value node is present and print its content
        if (valueNode.isMissingNode()) {
            System.out.println("Value node is missing in the response.");
        } else {
            System.out.println("Value Node: " + valueNode.toString());
        }

        for (JsonNode userNode : valueNode) {
            String displayName = userNode.path("user").path("displayName").asText();
            String email = userNode.path("user").path("mailAddress").asText();
            userMap.put(displayName, email);
        }

        System.out.println("Parsed User Map: " + userMap);
        return userMap;
    }

    public String fetchUniqueWorkItemTypes(String jiraProjectKey, String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        Map<String, String> orgAndProject = extractOrganizationAndProjectFromBaseUrl(adoApiBaseUrl);
        String organization = orgAndProject.get("organization");
        String projectName = orgAndProject.get("projectName");
        String url = "https://dev.azure.com/" + organization + "/_apis/projects/" + projectName + "?includeCapabilities=true&api-version=7.1";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseWorkItemTypesResponse(response.getBody());
    }

    private String parseWorkItemTypesResponse(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode templateTypeIdNode = rootNode.path("capabilities").path("processTemplate").path("templateTypeId");
        return templateTypeIdNode.asText();
    }

//   ####


    public List<String> fetchWorkItemTypes(String jiraProjectKey, String token) throws Exception {
        initializeUrlsFromDataSource(jiraProjectKey, token);
        Map<String, String> orgAndProject = extractOrganizationAndProjectFromBaseUrl(adoApiBaseUrl);
        String organization = orgAndProject.get("organization");


        // Fetch templateTypeId
        String templateTypeId = fetchUniqueWorkItemTypes(jiraProjectKey, token);

        // Fetch work item types using templateTypeId
        String url = "https://dev.azure.com/" + organization + "/_apis/work/processes/" + templateTypeId + "/workitemtypes?api-version=7.1";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseWorkItemNames(response.getBody());
    }

    private List<String> parseWorkItemNames(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode valueNode = rootNode.path("value");
        List<String> workItemNames = new ArrayList<>();
        for (JsonNode workItemNode : valueNode) {
            workItemNames.add(workItemNode.path("name").asText());
        }
        return workItemNames;
    }
}
