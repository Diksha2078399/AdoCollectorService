package com.ADO.ADOPersonal.db.repo;

import com.ADO.ADOPersonal.rts.Defect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DefectRepository extends MongoRepository<Defect, String>{


    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate = null;

    void deleteAllByProjectid(String projectName);

    Optional<Defect> findByDefid(String key);

    Defect findFirstByDefid(String defid);
}
