package com.mongodbdemo.authservice.service;

import com.mongodbdemo.authservice.dto.AuthResponse;
import com.mongodbdemo.authservice.dto.AuthValidationRequest;
import com.mongodbdemo.authservice.entity.UserCredential;
import com.mongodbdemo.authservice.repository.UserCredentialRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.*;

/**
 * Service class for handling JSON Web Token (JWT) operations.
 * Provides methods for generating access tokens and validating them.
 */
@Service
public class JwtService {

    @Value("${app.secret}")
    String secret;

    private static final long TOKEN_VALIDITY = 1000 * 60 * 5; // 5 minutes

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    /**
     * Generates an access token for the specified user.
     *
     * @param userCredential the user for whom the access token is to be generated.
     * @return the generated access token.
     */
    public String generateAccessToken(UserCredential userCredential) {
        Map<String, Object> claims = Map.of(
                "roles", CollectionUtils.isEmpty(userCredential.getRoles()) ? List.of() : userCredential.getRoles(),
                "username", userCredential.getUsername());
        return createToken(claims, userCredential.getId());
    }

    /**
     * Creates a JWT token with the specified claims and subject.
     *
     * @param claims the claims to be included in the token.
     * @param userId the subject (user ID) of the token.
     * @return the created JWT token.
     */
    private String createToken(Map<String, Object> claims, String userId) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Retrieves the signing key used to sign the JWT.
     *
     * @return the signing key.
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates the provided JWT token and checks if it has the required roles.
     *
     * @param validationRequest the request containing the token and required roles.
     * @throws ResponseStatusException if the token is invalid, expired, or if the roles are insufficient.
     */
    public AuthResponse validateToken(AuthValidationRequest validationRequest) {
        Claims claims;
        try {
            claims = parseClaims(validationRequest.getAccessToken());
        } catch (ExpiredJwtException | SignatureException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        validateUserDetails(claims);
        validateRoles(claims, validationRequest.getRoles());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUserId(claims.getSubject());
        return authResponse;
    }

    void validateUserDetails(Claims claims) {
        // Extract user ID and username from the claims
        String userId = String.valueOf(claims.getSubject());
        String username = String.valueOf(claims.get("username"));

        // Retrieve the user credential from the repository
        UserCredential userCredential = userCredentialRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Provided User is not present"));

        // Validate the username
        if (!username.equals(userCredential.getUsername())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid User details provided");
        }
    }


    /*
     * Validates if the roles present in the token match the required roles.
     *
     * @param claims the claims extracted from the JWT.
     * @param requiredRoles the roles that are required.
     * @throws ResponseStatusException if the roles do not meet the requirements.
     */
    void validateRoles(Claims claims, List<String> requiredRoles) {
        List<String> roles = claims.get("roles", List.class);

        if (!requiredRoles.isEmpty() && ((roles == null) ||
                roles.stream().noneMatch(requiredRoles::contains))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient roles");
        }
    }

    /**
     * Parses the claims from the provided JWT token.
     *
     * @param token the JWT token to be parsed.
     * @return the claims extracted from the token.
     */
    Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(getSignKey()).build()
                .parseClaimsJws(token).getBody();
    }
}
