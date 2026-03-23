package com.studyplanner.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response containing best study times for a subject
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestTimesResponse {
    private String subject;
    private List<PredictionResponse> bestTimes;
}
