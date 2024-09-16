package com.mongodbdemo.authservice.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "user_credential")
@AllArgsConstructor
@NoArgsConstructor
public class UserCredential {

    private String id;
    private String username;
    private String password;
    private Set<String> roles;

}
