package com.demo.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "test-secret-key-for-testing-only-32chars!!");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L);

        userDetails = new User("testuser", "password", Collections.emptyList());
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void generateToken_success() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from token")
    void extractUsername_success() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should validate token successfully")
    void isTokenValid_success() {
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should reject token for different user")
    void isTokenValid_wrongUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException for expired token")
    void isTokenValid_expired() {
        // Gera token já expirado (-1000ms no passado)
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L);
        String expiredToken = jwtService.generateToken(userDetails);

        // JJWT lança ExpiredJwtException ao tentar fazer parse de token expirado
        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }
}