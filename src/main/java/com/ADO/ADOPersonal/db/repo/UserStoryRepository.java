package com.ADO.ADOPersonal.db.repo;

import com.ADO.ADOPersonal.rts.Userstories;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserStoryRepository extends MongoRepository<Userstories, String> {


    void deleteAllByProjectid(String projectName);

    Optional<Userstories> findByUserStoryID(String key);

    Userstories findFirstByUserStoryID(String userStoryID);
}
