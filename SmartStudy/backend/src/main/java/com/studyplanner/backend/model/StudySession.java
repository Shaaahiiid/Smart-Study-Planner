package com.studyplanner.backend.model;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a study session
 * Maps to 'study_sessions' table in database
 */
@Entity
@Table(name = "study_sessions")
@Data                    // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Lombok: Generates no-args constructor
@AllArgsConstructor      // Lombok: Generates constructor with all fields
public class StudySession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String subject;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "focus_rating", nullable = false)
    private Integer focusRating;    // 1-10 scale
    
    @Column(name = "duration")
    private Integer duration;        // in minutes
    
    @Column(name = "hour_of_day")
    private Integer hourOfDay;       // 0-23
    
    @Column(name = "day_of_week")
    private Integer dayOfWeek;       // 0-6 (0=Sunday)
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Automatically called before persisting entity to database
     * Calculates duration, hour, and day from start/end times
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        
        // Calculate duration in minutes
        if (this.startTime != null && this.endTime != null) {
            Duration diff = Duration.between(this.startTime, this.endTime);
            this.duration = (int) diff.toMinutes();
        }
        
        // Extract hour and day for ML features
        if (this.startTime != null) {
            this.hourOfDay = this.startTime.getHour();
            // Convert to 0=Sunday format (Python ML uses this)
            this.dayOfWeek = this.startTime.getDayOfWeek().getValue() % 7;
        }
    }
}
