package com.mongodbdemo.authservice.controller;

import com.mongodbdemo.authservice.dto.*;
import com.mongodbdemo.authservice.exception.DuplicateUserException;
import com.mongodbdemo.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addNewUserShouldReturnOkResponseWhenUserIsAddedSuccessfully() throws DuplicateUserException {
        UserCredentialCreateRequestDTO userCredential = new UserCredentialCreateRequestDTO();
        userCredential.setUsername("testuser@example.com");
        userCredential.setPassword("password123");

        doNothing().when(authService).saveUser(userCredential);

        ResponseEntity<ApiResponse> response = authController.addNewUser(userCredential);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User Successfully Added", response.getBody().message());
        verify(authService, times(1)).saveUser(userCredential);
    }

    @Test
    void addNewUserShouldThrowDuplicateUserExceptionWhenUserAlreadyExists() throws DuplicateUserException {
        UserCredentialCreateRequestDTO userCredential = new UserCredentialCreateRequestDTO();
        userCredential.setUsername("existinguser@example.com");
        userCredential.setPassword("password123");

        doThrow(new DuplicateUserException("User already exists")).when(authService).saveUser(userCredential);

        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> {
            authController.addNewUser(userCredential);
        });

        assertEquals("User already exists", exception.getMessage());
        verify(authService, times(1)).saveUser(userCredential);
    }

    @Test
    void getTokenShouldReturnOkResponseWhenCredentialsAreValid() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("validuser@example.com");
        authRequest.setPassword("validpassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        AuthResponse authResponse = new AuthResponse("new_access_token",
                "new_refresh_token", UUID.randomUUID().toString(), List.of());
        when(authService.generateToken(authRequest.getUsername())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.getToken(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new_access_token", response.getBody().getAccessToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authService, times(1)).generateToken(authRequest.getUsername());
    }

    @Test
    void validateShouldReturnOkResponseWhenTokenIsValid() {
        AuthValidationRequest authValidationRequest = new AuthValidationRequest();
        authValidationRequest.setAccessToken("valid_token");

        AuthResponse authResponse = new AuthResponse("access_token", "refresh_token", "userId", List.of("ROLE_USER"));
        when(authService.validate(authValidationRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.validate(authValidationRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access_token", response.getBody().getAccessToken());
        verify(authService, times(1)).validate(authValidationRequest);
    }

    @Test
    void validateShouldThrowResponseStatusExceptionWhenTokenIsInvalid() {
        AuthValidationRequest authValidationRequest = new AuthValidationRequest();
        authValidationRequest.setAccessToken("invalid_token");

        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token")).when(authService).validate(authValidationRequest);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authController.validate(authValidationRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid Token", exception.getReason());
        verify(authService, times(1)).validate(authValidationRequest);
    }

    @Test
    void getNewAccessTokenShouldReturnOkResponseWhenRefreshTokenIsValid() {
        String refreshToken = "valid_refresh_token";
        AuthResponse authResponse = new AuthResponse("new_access_token", "new_refresh_token", UUID.randomUUID().toString(), List.of());

        when(authService.getNewAccessToken(refreshToken)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.getNewAccessToken(refreshToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new_access_token", response.getBody().getAccessToken());
        verify(authService, times(1)).getNewAccessToken(refreshToken);
    }

    @Test
    void getNewAccessTokenShouldThrowResponseStatusExceptionWhenRefreshTokenIsInvalid() {
        String refreshToken = "invalid_refresh_token";

        when(authService.getNewAccessToken(refreshToken)).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authController.getNewAccessToken(refreshToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid Refresh Token", exception.getReason());
        verify(authService, times(1)).getNewAccessToken(refreshToken);
    }

    @Test
    void getTokenShouldThrowResponseStatusExceptionWhenCredentialsAreInvalid() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("invaliduser@example.com");
        authRequest.setPassword("invalidpassword");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authController.getToken(authRequest);
        });

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid User Credentials provided", exception.getReason());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authService, times(0)).generateToken(anyString());
    }
}
