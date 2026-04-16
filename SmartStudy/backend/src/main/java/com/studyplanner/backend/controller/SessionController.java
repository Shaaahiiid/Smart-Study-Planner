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
import com.studyplanner.backend.security.JwtUtil;
import com.studyplanner.backend.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST Controller for study session operations
 * All endpoints start with /api/sessions
 * All operations are filtered by the logged-in user
 */
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(originPatterns = "*")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * CREATE: Post a new study session
     * POST /api/sessions
     */
    @PostMapping
    public ResponseEntity<?> createSession(@Valid @RequestBody SessionDTO sessionDTO, HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            StudySession session = sessionService.createSession(sessionDTO, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session created successfully");
            response.put("session", session);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create session: " + e.getMessage()));
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
            @RequestParam(required = false, defaultValue = "false") boolean recent,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            List<StudySession> sessions;

            if (recent) {
                sessions = sessionService.getRecentSessions(userId);
            } else if (subject != null && !subject.isEmpty()) {
                sessions = sessionService.getSessionsBySubject(userId, subject);
            } else {
                sessions = sessionService.getAllSessions(userId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessions);
            response.put("count", sessions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch sessions: " + e.getMessage()));
        }
    }

    /**
     * READ: Get single session by ID
     * GET /api/sessions/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            StudySession session = sessionService.getSessionById(id, userId);
            return ResponseEntity.ok(session);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE: Delete a session
     * DELETE /api/sessions/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            sessionService.deleteSession(id, userId);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET: Get statistics for the logged-in user
     * GET /api/sessions/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please login first"));
            }

            Map<String, Object> stats = sessionService.getStatistics(userId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics: " + e.getMessage()));
        }
    }
}