package com.studyplanner.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Simple JWT utility for generating and validating tokens
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token for a user
     */
    public String generateToken(Long userId, String email, String name) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("name", name)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract all claims from token
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract userId from token
     */
    public Long getUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /**
     * Check if token is valid
     */
    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper: Extract userId from HttpServletRequest
     * Controllers can call this directly to get the logged-in user's ID
     * Returns null if no valid token found
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        try {
            return getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
