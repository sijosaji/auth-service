package com.mongodbdemo.authservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AuthValidationRequest {
    @NotNull
    @NotEmpty
    private String accessToken;
    private List<String> roles;
}
