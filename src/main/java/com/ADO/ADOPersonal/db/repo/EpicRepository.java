package com.ADO.ADOPersonal.db.repo;

import com.ADO.ADOPersonal.rts.Epic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EpicRepository extends MongoRepository<Epic, String> {
    Optional<Epic> findById(String id);
}
