package com.studyplanner.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studyplanner.backend.dto.SessionDTO;
import com.studyplanner.backend.model.StudySession;
import com.studyplanner.backend.service.SessionService;

import jakarta.validation.Valid;
/**
 * REST Controller for study session operations
 * All endpoints start with /api/sessions
 */
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(originPatterns = "*")  // Allow all origins (for development)
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * CREATE: Post a new study session
     * POST /api/sessions
     */
    @PostMapping
    public ResponseEntity<?> createSession(@Valid @RequestBody SessionDTO sessionDTO) {
        try {
            StudySession session = sessionService.createSession(sessionDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session created successfully");
            response.put("session", session);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * READ: Get all sessions or filter by subject
     * GET /api/sessions
     * GET /api/sessions?subject=Mathematics
     * GET /api/sessions?recent=true
     */
    @GetMapping
    public ResponseEntity<?> getSessions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false, defaultValue = "false") boolean recent) {
        try {
            List<StudySession> sessions;

            if (recent) {
                sessions = sessionService.getRecentSessions();
            } else if (subject != null && !subject.isEmpty()) {
                sessions = sessionService.getSessionsBySubject(subject);
            } else {
                sessions = sessionService.getAllSessions();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessions);
            response.put("count", sessions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * READ: Get single session by ID
     * GET /api/sessions/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Long id) {
        try {
            StudySession session = sessionService.getSessionById(id);
            return ResponseEntity.ok(session);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * DELETE: Delete a session
     * DELETE /api/sessions/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            sessionService.deleteSession(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * GET: Get statistics
     * GET /api/sessions/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = sessionService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

    }
}