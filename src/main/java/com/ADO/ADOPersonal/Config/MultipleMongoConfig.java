package com.ADO.ADOPersonal.Config;

import com.ADO.ADOPersonal.db.repo.ReleaseRepository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@EnableMongoRepositories(basePackageClasses = ReleaseRepository.class, mongoTemplateRef = "primaryMongoTemplate")
@Configuration
public class MultipleMongoConfig {

    @Primary
    @Bean(name = "primaryMongoTemplate")
    public MongoTemplate primaryMongoTemplate(@Value("${spring.data.mongodb.uri:mongodb://localhost:27017/test}") String uri) {
        MongoClient mongoClient = MongoClients.create(uri);
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, "test"));
    }

    @Bean(name = "secondaryMongoTemplate")
    public MongoTemplate secondaryMongoTemplate(@Value("${spring.data.mongodb.secondary-uri:mongodb://localhost:27017/adoUrlDetails}") String uri) {
        MongoClient mongoClient = MongoClients.create(uri);
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, "adoUrlDetails"));
    }

}

