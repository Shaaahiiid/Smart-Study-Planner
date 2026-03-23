package com.studyplanner.backend.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.studyplanner.backend.dto.BestTimesResponse;
import com.studyplanner.backend.dto.PredictionResponse;
import com.studyplanner.backend.model.StudySession;

/**
 * Client for communicating with Python ML service
 * Makes HTTP requests to Flask API
 */
@Component
public class PythonMLClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${ml.service.url}")
    private String mlServiceUrl;  // http://localhost:5001
    
    /**
     * Get single prediction from Python ML service
     */
    public PredictionResponse getPrediction(int hour, int day, String subject) {
        try {
            String url = mlServiceUrl + "/predict";
            
            // Prepare request body
            Map<String, Object> request = new HashMap<>();
            request.put("hour", hour);
            request.put("day", day);
            request.put("subject", subject);
            
            // Make POST request to Python service
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    url, 
                    request, 
                    Map.class
            );

            if (response == null) {
                throw new RuntimeException("Empty response from ML service");
            }

            Object success = response.get("success");
            if (success instanceof Boolean && !(Boolean) success) {
                throw new RuntimeException(String.valueOf(response.getOrDefault("error", "Prediction failed")));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> prediction = (Map<String, Object>) response.get("prediction");
            if (prediction == null) {
                throw new RuntimeException("Invalid prediction response format from ML service");
            }

            return mapPrediction(prediction);
            
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get prediction from ML service: " + e.getMessage());
        }
    }
    
    /**
     * Get best study times for a subject
     */
    public BestTimesResponse getBestTimes(String subject, int topN) {
        try {
            String url = mlServiceUrl + "/predict/best-times";
            
            // Prepare request body
            Map<String, Object> request = new HashMap<>();
            request.put("subject", subject);
            request.put("top_n", topN);
            
            // Make POST request to Python service
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    url, 
                    request, 
                    Map.class
            );

            if (response == null) {
                throw new RuntimeException("Empty response from ML service");
            }

            Object success = response.get("success");
            if (success instanceof Boolean && !(Boolean) success) {
                throw new RuntimeException(String.valueOf(response.getOrDefault("error", "Best times prediction failed")));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawBestTimes = (List<Map<String, Object>>) response.get("best_times");

            List<PredictionResponse> bestTimes = new ArrayList<>();
            if (rawBestTimes != null) {
                for (Map<String, Object> item : rawBestTimes) {
                    bestTimes.add(mapPrediction(item));
                }
            }

            BestTimesResponse bestTimesResponse = new BestTimesResponse();
            bestTimesResponse.setSubject(String.valueOf(response.getOrDefault("subject", subject)));
            bestTimesResponse.setBestTimes(bestTimes);
            
            return bestTimesResponse;
            
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get best times from ML service: " + e.getMessage());
        }
    }
    
    /**
     * Trigger model training on Python service
     */
    public Map<String, Object> trainModel(List<StudySession> sessions) {
        try {
            String url = mlServiceUrl + "/train";
            
            // Prepare request body
            Map<String, Object> request = new HashMap<>();
            request.put("sessions", sessions);
            
            // Make POST request to Python service
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    url, 
                    request, 
                    Map.class
            );
            
            return response;
            
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to train model: " + e.getMessage());
        }
    }
    
    /**
     * Check if ML service is running
     */
    public boolean isMLServiceAvailable() {
        try {
            String url = mlServiceUrl + "/";
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }

    private PredictionResponse mapPrediction(Map<String, Object> source) {
        PredictionResponse response = new PredictionResponse();
        response.setPredictedFocus(toDouble(source.get("predicted_focus")));
        response.setConfidence(toStringValue(source.get("confidence")));
        response.setHour(toInteger(source.get("hour")));
        response.setDay(toInteger(source.get("day")));
        response.setSubject(toStringValue(source.get("subject")));
        response.setDayName(toStringValue(source.get("day_name")));
        response.setTimeDisplay(toStringValue(source.get("time_display")));
        return response;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}