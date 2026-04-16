package com.studyplanner.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studyplanner.backend.dto.BestTimesResponse;
import com.studyplanner.backend.dto.PredictionResponse;
import com.studyplanner.backend.security.JwtUtil;
import com.studyplanner.backend.service.MLPredictionService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST Controller for ML prediction operations
 * All endpoints start with /api/predictions
 */
@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(originPatterns = "*")
public class PredictionController {
    
    @Autowired
    private MLPredictionService mlPredictionService;

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * GET: Single prediction
     */
    @GetMapping("/predict")
    public ResponseEntity<?> getPrediction(
            @RequestParam int hour,
            @RequestParam int day,
            @RequestParam String subject,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            if (hour < 0 || hour > 23) {
                return ResponseEntity.badRequest().body(Map.of("error", "Hour must be between 0 and 23"));
            }
            if (day < 0 || day > 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Day must be between 0 (Sunday) and 6 (Saturday)"));
            }
            
            PredictionResponse prediction = mlPredictionService.getPrediction(hour, day, subject);
            return ResponseEntity.ok(prediction);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET: Best study times
     */
    @GetMapping("/best-times")
    public ResponseEntity<?> getBestTimes(
            @RequestParam String subject,
            @RequestParam(required = false) Integer top_n,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            BestTimesResponse bestTimes = mlPredictionService.getBestTimes(subject, top_n);
            return ResponseEntity.ok(bestTimes);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST: Train the ML model with the user's data only
     */
    @PostMapping("/train")
    public ResponseEntity<?> trainModel(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            Map<String, Object> result = mlPredictionService.trainModel(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Model trained successfully");
            response.put("details", result);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET: ML service status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            Map<String, Object> status = mlPredictionService.getMLServiceStatus(userId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get ML service status: " + e.getMessage()));
        }
    }
}
