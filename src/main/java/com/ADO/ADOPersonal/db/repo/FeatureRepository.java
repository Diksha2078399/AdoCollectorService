package com.ADO.ADOPersonal.db.repo;

import com.ADO.ADOPersonal.rts.Feature;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FeatureRepository extends MongoRepository<Feature, String> {
    Optional<Feature> findById(String id);
}
