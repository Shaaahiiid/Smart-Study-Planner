package com.studyplanner.backend.service;

import com.studyplanner.backend.dto.SessionDTO;
import com.studyplanner.backend.model.StudySession;
import com.studyplanner.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for study session operations
 * Contains business logic (validation, calculations, etc.)
 */
@Service
@Transactional
public class SessionService {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    /**
     * Create a new study session
     */
    public StudySession createSession(SessionDTO dto) {
        // Validate end time is after start time
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Convert DTO to Entity
        StudySession session = new StudySession();
        session.setSubject(dto.getSubject());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setFocusRating(dto.getFocusRating());
        session.setNotes(dto.getNotes());
        
        // Save to database (duration, hour, day calculated in @PrePersist)
        return sessionRepository.save(session);
    }
    
    /**
     * Get all sessions
     */
    public List<StudySession> getAllSessions() {
        return sessionRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get recent sessions (latest 10)
     */
    public List<StudySession> getRecentSessions() {
        return sessionRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    /**
     * Get sessions by subject
     */
    public List<StudySession> getSessionsBySubject(String subject) {
        return sessionRepository.findBySubject(subject);
    }
    
    /**
     * Get single session by ID
     */
    public StudySession getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }
    
    /**
     * Delete a session
     */
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Session not found with id: " + id);
        }
        sessionRepository.deleteById(id);
    }
    
    /**
     * Get overall statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total sessions
        long totalSessions = sessionRepository.count();
        stats.put("totalSessions", totalSessions);
        
        if (totalSessions == 0) {
            stats.put("totalHours", 0.0);
            stats.put("averageFocus", 0.0);
            return stats;
        }
        
        // Total hours studied
        Long totalMinutes = sessionRepository.getTotalStudyTimeMinutes();
        double totalHours = totalMinutes != null ? Math.round(totalMinutes / 60.0 * 10.0) / 10.0 : 0.0;
        stats.put("totalHours", totalHours);
        
        // Average focus rating
        List<StudySession> allSessions = sessionRepository.findAll();
        double avgFocus = allSessions.stream()
                .mapToInt(StudySession::getFocusRating)
                .average()
                .orElse(0.0);
        stats.put("averageFocus", Math.round(avgFocus * 10.0) / 10.0);
        
        // Statistics by subject
        List<Object[]> bySubject = sessionRepository.getAverageFocusBySubject();
        stats.put("bySubject", bySubject);
        
        return stats;
    }
}