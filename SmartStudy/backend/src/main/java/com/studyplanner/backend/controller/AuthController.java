package com.studyplanner.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studyplanner.backend.model.User;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.security.JwtUtil;

/**
 * REST Controller for authentication
 * POST /api/auth/register — create account
 * POST /api/auth/login    — login & get JWT token
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user
     * Request body: { "name": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                return badRequest("Name is required");
            }
            if (email == null || email.trim().isEmpty()) {
                return badRequest("Email is required");
            }
            if (password == null || password.length() < 6) {
                return badRequest("Password must be at least 6 characters");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                return badRequest("Email is already registered");
            }

            // Create user with hashed password
            User user = new User();
            user.setName(name.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(password));

            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Registration successful! Please login.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Login and get JWT token
     * Request body: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return badRequest("Email and password are required");
            }

            // Find user by email
            User user = userRepository.findByEmail(email.trim().toLowerCase())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }

            // Check password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("token", token);
            response.put("name", user.getName());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
