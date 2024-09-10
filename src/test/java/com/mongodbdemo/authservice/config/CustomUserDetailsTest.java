package com.mongodbdemo.authservice.config;

import com.mongodbdemo.authservice.entity.UserCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CustomUserDetailsTest {

    private CustomUserDetails customUserDetails;
    private UserCredential userCredential;

    @BeforeEach
    public void setUp() {
        userCredential = new UserCredential();
        userCredential.setUsername("testuser");
        userCredential.setPassword("password123");

        customUserDetails = new CustomUserDetails(userCredential);
    }

    @Test
    public void getUsernameShouldReturnUsername() {
        assertEquals("testuser", customUserDetails.getUsername());
    }

    @Test
    public void getPasswordShouldReturnPassword() {
        assertEquals("password123", customUserDetails.getPassword());
    }

    @Test
    public void getAuthoritiesShouldReturnNull() {
        assertNull(customUserDetails.getAuthorities());
    }
}
