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
import com.studyplanner.backend.service.MLPredictionService;

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
    
    /**
     * GET: Single prediction
     * GET /api/predictions/predict?hour=10&day=2&subject=Mathematics
     */
    @GetMapping("/predict")
    public ResponseEntity<?> getPrediction(
            @RequestParam int hour,
            @RequestParam int day,
            @RequestParam String subject) {
        try {
            // Validate inputs
            if (hour < 0 || hour > 23) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Hour must be between 0 and 23");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (day < 0 || day > 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Day must be between 0 (Sunday) and 6 (Saturday)");
                return ResponseEntity.badRequest().body(error);
            }
            
            PredictionResponse prediction = mlPredictionService.getPrediction(hour, day, subject);
            return ResponseEntity.ok(prediction);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * GET: Best study times
     * GET /api/predictions/best-times?subject=Mathematics&top_n=5
     */
    @GetMapping("/best-times")
    public ResponseEntity<?> getBestTimes(
            @RequestParam String subject,
            @RequestParam(required = false) Integer top_n) {
        try {
            BestTimesResponse bestTimes = mlPredictionService.getBestTimes(subject, top_n);
            return ResponseEntity.ok(bestTimes);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * POST: Train the ML model
     * POST /api/predictions/train
     */
    @PostMapping("/train")
    public ResponseEntity<?> trainModel() {
        try {
            Map<String, Object> result = mlPredictionService.trainModel();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Model trained successfully");
            response.put("details", result);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * GET: ML service status
     * GET /api/predictions/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            Map<String, Object> status = mlPredictionService.getMLServiceStatus();
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get ML service status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
