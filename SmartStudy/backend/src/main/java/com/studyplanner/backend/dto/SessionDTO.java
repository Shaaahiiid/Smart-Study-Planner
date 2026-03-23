package com.studyplanner.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating/updating study sessions
 * Used for API request/response (cleaner than using entity directly)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    
    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must be less than 100 characters")
    private String subject;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Focus rating is required")
    @Min(value = 1, message = "Focus rating must be at least 1")
    @Max(value = 10, message = "Focus rating must be at most 10")
    private Integer focusRating;
    
    private String notes;
}
