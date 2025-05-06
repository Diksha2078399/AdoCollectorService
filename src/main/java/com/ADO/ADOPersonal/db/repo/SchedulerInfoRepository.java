package com.ADO.ADOPersonal.db.repo;


import com.ADO.ADOPersonal.metadata.SchedulerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SchedulerInfoRepository extends MongoRepository<SchedulerInfo, String> {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate = null;
    Optional<SchedulerInfo> findFirstByToolName(String toolName);

    Optional<SchedulerInfo> findFirstByToolNameAndProjectId(String toolName, String projectId);

    List<SchedulerInfo> findByProjectId(String projectId);

    SchedulerInfo findByToolName(String toolName);

    SchedulerInfo findByToolNameAndJiraProjectKey(String toolName, String projectKey);

    Optional<SchedulerInfo> findFirstByToolNameAndJiraProjectKey(String toolName, String jiraProjectKey);

    List<SchedulerInfo> findByJiraProjectKey(String jiraProjectKey);
}

