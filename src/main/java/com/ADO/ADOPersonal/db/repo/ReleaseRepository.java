package com.ADO.ADOPersonal.db.repo;

import com.ADO.ADOPersonal.rts.ReleaseTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReleaseRepository extends MongoRepository<ReleaseTracker, String> {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate = null;
    void deleteAllByProjectid(String projectName);

    void deleteAllByJiraProjectKey(String key);

    List<ReleaseTracker> findByJiraProjectKey(String key);

    ReleaseTracker findByReleaseName(String releaseName);
}

