package com.studyplanner.backend.service;

import com.studyplanner.backend.client.PythonMLClient;
import com.studyplanner.backend.dto.BestTimesResponse;
import com.studyplanner.backend.dto.PredictionResponse;
import com.studyplanner.backend.model.StudySession;
import com.studyplanner.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for ML-related operations
 * Acts as a bridge between controllers and Python ML service
 */
@Service
public class MLPredictionService {
    
    @Autowired
    private PythonMLClient pythonMLClient;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    private static final int MIN_SESSIONS_FOR_TRAINING = 30;
    
    /**
     * Get focus prediction for specific time/subject
     */
    public PredictionResponse getPrediction(int hour, int day, String subject) {
        // Check if ML service is available
        if (!pythonMLClient.isMLServiceAvailable()) {
            throw new RuntimeException("ML service is not available. Please make sure Python ML service is running.");
        }
        
        return pythonMLClient.getPrediction(hour, day, subject);
    }
    
    /**
     * Get best study times for a subject
     */
    public BestTimesResponse getBestTimes(String subject, Integer topN) {
        // Check if ML service is available
        if (!pythonMLClient.isMLServiceAvailable()) {
            throw new RuntimeException("ML service is not available. Please make sure Python ML service is running.");
        }
        
        int n = (topN != null && topN > 0) ? topN : 5;
        return pythonMLClient.getBestTimes(subject, n);
    }
    
    /**
     * Train the ML model
     */
    public Map<String, Object> trainModel() {
        // Get all sessions
        List<StudySession> sessions = sessionRepository.findAll();
        
        // Check if we have enough data
        if (sessions.size() < MIN_SESSIONS_FOR_TRAINING) {
            throw new RuntimeException(
                    String.format("Need at least %d sessions to train model. Current: %d", 
                            MIN_SESSIONS_FOR_TRAINING, sessions.size())
            );
        }
        
        // Check if ML service is available
        if (!pythonMLClient.isMLServiceAvailable()) {
            throw new RuntimeException("ML service is not available. Please make sure Python ML service is running.");
        }
        
        // Trigger training on Python service
        return pythonMLClient.trainModel(sessions);
    }
    
    /**
     * Check ML service status
     */
    public Map<String, Object> getMLServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        boolean available = pythonMLClient.isMLServiceAvailable();
        status.put("available", available);
        
        long sessionCount = sessionRepository.count();
        status.put("totalSessions", sessionCount);
        status.put("canTrain", sessionCount >= MIN_SESSIONS_FOR_TRAINING);
        status.put("minSessionsRequired", MIN_SESSIONS_FOR_TRAINING);
        
        return status;
    }

}
