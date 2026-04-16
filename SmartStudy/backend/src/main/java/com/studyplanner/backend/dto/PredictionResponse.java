package com.studyplanner.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from Python ML service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    
    private Double predictedFocus;
    private String confidence;
    private Integer hour;
    private Integer day;
    private String subject;
    private String dayName;
    private String timeDisplay;
}
