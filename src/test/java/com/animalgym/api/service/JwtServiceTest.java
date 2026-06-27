package com.animalgym.api.service;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "0a5b3c8d1e2f4a7b9c0d3e5f8a2b4c6d7e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8";
    private static final long EXPIRATION = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken("admin@animalgym.com", "ADMIN");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("admin@animalgym.com", jwtService.extractEmail(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService shortLived = new JwtService(SECRET, -1);
        String token = shortLived.generateToken("admin@animalgym.com", "ADMIN");

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void shouldExtractCorrectClaims() {
        String token = jwtService.generateToken("staff@animalgym.com", "STAFF");

        assertEquals("staff@animalgym.com", jwtService.extractEmail(token));
        assertEquals("STAFF", jwtService.extractRole(token));
    }
}
