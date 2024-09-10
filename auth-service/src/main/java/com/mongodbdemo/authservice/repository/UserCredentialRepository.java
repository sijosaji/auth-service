package com.mongodbdemo.authservice.repository;

import com.mongodbdemo.authservice.entity.UserCredential;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserCredentialRepository extends MongoRepository<UserCredential, String> {
    Optional<UserCredential> findByUsername(String email);
}
