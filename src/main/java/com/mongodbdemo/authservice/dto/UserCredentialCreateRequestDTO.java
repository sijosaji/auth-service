package com.mongodbdemo.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UserCredentialCreateRequestDTO {
    @Email
    @NotNull
    private String username;
    @NotNull
    private String password;
    private Set<String> roles;
}
