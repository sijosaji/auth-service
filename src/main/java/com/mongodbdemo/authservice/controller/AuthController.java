package com.mongodbdemo.authservice.controller;

import com.mongodbdemo.authservice.dto.*;
import com.mongodbdemo.authservice.exception.DuplicateUserException;
import com.mongodbdemo.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Controller for handling authentication and user registration requests.
 * Provides endpoints for user registration, token generation, token validation, and access token refreshing.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Registers a new user.
     *
     * @param userCredential the user credentials to be used for registration.
     * @return a response entity with a message indicating successful user addition.
     * @throws DuplicateUserException if a user with the same username already exists.
     */
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> addNewUser(@RequestBody @Valid UserCredentialCreateRequestDTO userCredential) throws DuplicateUserException {
        service.saveUser(userCredential);
        return ResponseEntity.ok(new ApiResponse("User Successfully Added"));
    }

    /**
     * Generates a new authentication token for the user.
     *
     * @param authRequest the authentication request containing the username and password.
     * @return a response entity containing the generated authentication token.
     * @throws ResponseStatusException if the authentication fails due to invalid credentials.
     */
    @PostMapping(path = "/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> getToken(@RequestBody @Valid AuthRequest authRequest) {
        Authentication authenticate = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                        authRequest.getPassword()));
        if (authenticate.isAuthenticated()) {
            return ResponseEntity.ok(service.generateToken(authRequest.getUsername()));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid User Credentials provided");
    }

    /**
     * Validates the provided authentication request.
     *
     * @param authValidationRequest the authentication validation request.
     * @return a response entity with a message indicating successful validation.
     */
    @PostMapping(path = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> validate(@RequestBody @Valid AuthValidationRequest authValidationRequest) {
        return ResponseEntity.ok(service.validate(authValidationRequest));
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @return a response entity containing the new access token.
     */
    @PutMapping(path = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> getNewAccessToken(@RequestBody @Valid RefreshRequest refreshRequest) {
        return ResponseEntity.ok(service.getNewAccessToken(refreshRequest.getRefreshToken()));
    }
}
