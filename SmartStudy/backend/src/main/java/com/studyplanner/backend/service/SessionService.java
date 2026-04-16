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

@Service
@Transactional
public class SessionService {
    
    @Autowired
    private SessionRepository sessionRepository;
    public StudySession createSession(SessionDTO dto, Long userId) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setSubject(dto.getSubject());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setFocusRating(dto.getFocusRating());
        session.setNotes(dto.getNotes());
        
        return sessionRepository.save(session);
    }
    
    /**
     * Get all sessions for a user
     */
    public List<StudySession> getAllSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get recent sessions for a user (latest 10)
     */
    public List<StudySession> getRecentSessions(Long userId) {
        return sessionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get sessions by subject for a user
     */
    public List<StudySession> getSessionsBySubject(Long userId, String subject) {
        return sessionRepository.findByUserIdAndSubject(userId, subject);
    }
    
    /**
     * Get single session by ID (only if it belongs to the user)
     */
    public StudySession getSessionById(Long id, Long userId) {
        StudySession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Session not found with id: " + id);
        }
        return session;
    }
    
    /**
     * Delete a session (only if it belongs to the user)
     */
    public void deleteSession(Long id, Long userId) {
        StudySession session = getSessionById(id, userId);
        sessionRepository.deleteById(session.getSessionId());
    }
    
    /**
     * Get statistics for a specific user
     */
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalSessions = sessionRepository.countByUserId(userId);
        stats.put("totalSessions", totalSessions);
        
        if (totalSessions == 0) {
            stats.put("totalHours", 0.0);
            stats.put("averageFocus", 0.0);
            return stats;
        }
        
        // Total hours studied
        Long totalMinutes = sessionRepository.getTotalStudyTimeMinutesForUser(userId);
        double totalHours = totalMinutes != null ? Math.round(totalMinutes / 60.0 * 10.0) / 10.0 : 0.0;
        stats.put("totalHours", totalHours);
        
        // Average focus rating
        List<StudySession> allSessions = sessionRepository.findAllByUserId(userId);
        double avgFocus = allSessions.stream()
                .mapToInt(StudySession::getFocusRating)
                .average()
                .orElse(0.0);
        stats.put("averageFocus", Math.round(avgFocus * 10.0) / 10.0);
        
        // Statistics by subject
        List<Object[]> bySubject = sessionRepository.getAverageFocusBySubjectForUser(userId);
        stats.put("bySubject", bySubject);
        
        return stats;
    }
}