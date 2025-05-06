package com.ADO.ADOPersonal.db.repo;

import com.ADO.ADOPersonal.rts.RTSTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RtsTestcasesRepo extends MongoRepository<RTSTestCase, String> {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate = null;

    void deleteAllByProjectid(String projectName);

    void deleteAllByJiraProjectKey(String jiraProjectKey);

    Optional<RTSTestCase> findByTestCaseID(String key);

    RTSTestCase findFirstByTestCaseID(String testCaseID);
}