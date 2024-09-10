package com.mongodbdemo.authservice.service;

import com.mongodbdemo.authservice.dto.AuthResponse;
import com.mongodbdemo.authservice.dto.AuthValidationRequest;
import com.mongodbdemo.authservice.dto.UserCredentialCreateRequestDTO;
import com.mongodbdemo.authservice.entity.RefreshToken;
import com.mongodbdemo.authservice.entity.UserCredential;
import com.mongodbdemo.authservice.exception.DuplicateUserException;
import com.mongodbdemo.authservice.repository.RefreshTokenRepository;
import com.mongodbdemo.authservice.repository.UserCredentialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service class for handling authentication-related operations.
 * Provides methods for user registration, token generation, token validation, and refreshing access tokens.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Logger logger = Logger.getLogger(AuthService.class.getName());
    private static final long REFRESH_TOKEN_VALIDITY = 60 * 15; // 15 minutes

    /**
     * Registers a new user in the system.
     *
     * @param userCredentialCreateRequestDTO the DTO containing user credentials for registration.
     * @throws DuplicateUserException if a user with the same username already exists.
     */
    public void saveUser(@Valid UserCredentialCreateRequestDTO userCredentialCreateRequestDTO) {
        userCredentialRepository.findByUsername(userCredentialCreateRequestDTO.getUsername())
                .ifPresent(u -> {
                    throw new DuplicateUserException("User with the same username already exists");
                });

        userCredentialRepository.save(mapCreateRequestToEntity(userCredentialCreateRequestDTO));
    }

    /**
     * Maps a {@link UserCredentialCreateRequestDTO} to a {@link UserCredential} entity.
     *
     * @param userCredentialCreateRequestDTO the DTO to be mapped.
     * @return the mapped {@link UserCredential} entity.
     */
    private UserCredential mapCreateRequestToEntity(UserCredentialCreateRequestDTO userCredentialCreateRequestDTO) {
        UserCredential userCredential = new UserCredential();
        userCredential.setUsername(userCredentialCreateRequestDTO.getUsername());
        userCredential.setPassword(passwordEncoder.encode(userCredentialCreateRequestDTO.getPassword()));
        userCredential.setRoles(userCredentialCreateRequestDTO.getRoles() == null ? new HashSet<>() :
                userCredentialCreateRequestDTO.getRoles());
        return userCredential;
    }

    /**
     * Generates authentication tokens for the specified user.
     *
     * @param username the username of the user for whom tokens are to be generated.
     * @return an {@link AuthResponse} containing the access token, refresh token, user ID, and roles.
     * @throws ResponseStatusException if the user is not found.
     */
    public AuthResponse generateToken(String username) {
        UserCredential userCredential = userCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new AuthResponse(
                jwtService.generateAccessToken(userCredential),
                generateRefreshToken(userCredential),
                userCredential.getId(),
                userCredential.getRoles() == null ? null :
                        userCredential.getRoles().stream().toList()
        );
    }

    /**
     * Validates the provided authentication request using the JWT service.
     *
     * @param authValidationRequest the authentication validation request containing the token to be validated.
     */
    public AuthResponse validate(AuthValidationRequest authValidationRequest) {
        return jwtService.validateToken(authValidationRequest);
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken the refresh token used to generate a new access token.
     * @return an {@link AuthResponse} containing the new access token, refresh token, user ID, and roles.
     * @throws ResponseStatusException if the refresh token is invalid or expired, or if the user is not found.
     */
    public AuthResponse getNewAccessToken(String refreshToken) {
        logger.fine("Refreshing token");
        RefreshToken refreshTokenObj = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshTokenObj.getExpiryDate().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        refreshTokenRepository.deleteById(refreshTokenObj.getId());

        UserCredential userCredential = userCredentialRepository.findById(refreshTokenObj.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new AuthResponse(
                jwtService.generateAccessToken(userCredential),
                generateRefreshToken(userCredential),
                userCredential.getId(),
                userCredential.getRoles().stream().toList()
        );
    }

    /**
     * Generates a new refresh token for the specified user.
     *
     * @param userCredential the user for whom the refresh token is to be generated.
     * @return the generated refresh token.
     */
    private String generateRefreshToken(UserCredential userCredential) {
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString().replace("-", ""),
                userCredential.getId(),
                Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY)
        );

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getId();
    }
}
