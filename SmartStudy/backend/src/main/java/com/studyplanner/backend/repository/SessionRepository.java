package com.studyplanner.backend.repository;

import com.studyplanner.backend.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StudySession entity
 * JpaRepository provides built-in methods: save, findAll, findById, delete, etc.
 */
@Repository
public interface SessionRepository extends JpaRepository<StudySession, Long> {
    
    /**
     * Find all sessions for a specific subject
     * Spring auto-generates SQL: SELECT * FROM study_sessions WHERE subject = ?
     */
    List<StudySession> findBySubject(String subject);
    
    /**
     * Find all sessions ordered by most recent first
     */
    List<StudySession> findAllByOrderByCreatedAtDesc();
    
    /**
     * Get latest N sessions
     */
    List<StudySession> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * Custom query: Get average focus rating by subject
     */
    @Query("SELECT s.subject, AVG(s.focusRating) FROM StudySession s GROUP BY s.subject")
    List<Object[]> getAverageFocusBySubject();
    
    /**
     * Custom query: Get total study time (sum of durations)
     */
    @Query("SELECT SUM(s.duration) FROM StudySession s")
    Long getTotalStudyTimeMinutes();
    
    /**
     * Count sessions by subject
     */
    long countBySubject(String subject);
}