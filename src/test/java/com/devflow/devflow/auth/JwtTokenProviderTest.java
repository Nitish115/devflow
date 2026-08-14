package com.devflow.devflow.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "test_secret_key_for_unit_testing_which_is_at_least_32_bytes_long_1234567890";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    void testGenerateAndValidateToken() {
        String email = "user@example.com";
        Role role = Role.PROJECT_MANAGER;

        String token = jwtTokenProvider.generateToken(email, role);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(role, jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, -1000); // Expiration in the past
        String token = shortLivedProvider.generateToken("user@example.com", Role.DEVELOPER);

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
