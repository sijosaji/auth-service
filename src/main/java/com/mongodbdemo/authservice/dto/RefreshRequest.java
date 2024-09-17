package com.mongodbdemo.authservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RefreshRequest {
    @NotNull
    @NotEmpty
    private String refreshToken;
}
