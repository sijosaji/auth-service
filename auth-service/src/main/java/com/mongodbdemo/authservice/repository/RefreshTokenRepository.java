package com.mongodbdemo.authservice.repository;

import com.mongodbdemo.authservice.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
}
