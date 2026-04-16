package com.studyplanner.backend.repository;

import com.studyplanner.backend.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StudySession entity
 */
@Repository
public interface SessionRepository extends JpaRepository<StudySession, Long> {
    
    // ===== User-filtered queries =====
    
    List<StudySession> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<StudySession> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<StudySession> findByUserIdAndSubject(Long userId, String subject);
    
    List<StudySession> findAllByUserId(Long userId);
    
    long countByUserId(Long userId);
    
    @Query("SELECT s.subject, AVG(s.focusRating) FROM StudySession s WHERE s.userId = :userId GROUP BY s.subject")
    List<Object[]> getAverageFocusBySubjectForUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(s.duration) FROM StudySession s WHERE s.userId = :userId")
    Long getTotalStudyTimeMinutesForUser(@Param("userId") Long userId);
    
    // ===== Original queries (kept for backward compatibility) =====
    
    List<StudySession> findBySubject(String subject);
    
    List<StudySession> findAllByOrderByCreatedAtDesc();
    
    List<StudySession> findTop10ByOrderByCreatedAtDesc();
    
    @Query("SELECT s.subject, AVG(s.focusRating) FROM StudySession s GROUP BY s.subject")
    List<Object[]> getAverageFocusBySubject();
    
    @Query("SELECT SUM(s.duration) FROM StudySession s")
    Long getTotalStudyTimeMinutes();
    
    long countBySubject(String subject);
}