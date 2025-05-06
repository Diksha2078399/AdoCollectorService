package com.ADO.ADOPersonal.db.repo;


import com.ADO.ADOPersonal.rts.TestExecutionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestCaseExecutionHistoryRepo extends MongoRepository<TestExecutionHistory, String>{

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate = null;

    void deleteAllByProjectId(String projectId);

    void deleteAllByJiraProjectKey(String projectId);

    TestExecutionHistory findFirstByTestCaseId(String testCaseId);
}
