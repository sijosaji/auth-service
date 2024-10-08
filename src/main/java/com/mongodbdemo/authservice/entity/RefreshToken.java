package com.mongodbdemo.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;

    private String userId;
    @Indexed(name = "expiryDate", expireAfterSeconds = 0) // TTL index to delete the document after expiryDate
    private Instant expiryDate;
}
