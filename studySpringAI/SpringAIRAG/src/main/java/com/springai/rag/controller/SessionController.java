package com.springai.rag.controller;

import com.springai.rag.memory.SessionManager;
import com.springai.rag.memory.SessionManager.CompressionResult;
import com.springai.rag.memory.SessionManager.SessionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/memory/session")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    private final SessionManager sessionManager;

    public SessionController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initSession(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");
        String userId = request.get("userId");

        log.info("========================================");
        log.info("[API] POST /memory/session/init");
        log.info("[API] Conversation ID: {}", conversationId);
        log.info("[API] User ID: {}", userId);

        SessionStatus status = sessionManager.initSession(conversationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("loadedMessages", status.getShortTermCount());
        result.put("sessionStatus", status.toMap());

        log.info("[API] Session initialized with {} messages", status.getShortTermCount());
        log.info("========================================");

        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(@RequestParam("conversationId") String conversationId) {
        log.info("========================================");
        log.info("[API] GET /memory/session/status");
        log.info("[API] Conversation ID: {}", conversationId);

        SessionStatus status = sessionManager.getSessionStatus(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionStatus", status.toMap());

        log.info("[API] Session status: {} messages, dirty: {}", status.getShortTermCount(), status.isDirty());
        log.info("========================================");

        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveSession(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");

        log.info("========================================");
        log.info("[API] POST /memory/session/save");
        log.info("[API] Conversation ID: {}", conversationId);

        CompressionResult compressionResult = sessionManager.saveAndCompress(conversationId);
        SessionStatus status = sessionManager.getSessionStatus(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("compressionResult", compressionResult.toMap());
        result.put("sessionStatus", status.toMap());

        log.info("[API] Session saved. Compression ratio: {}", compressionResult.getCompressionRatio());
        log.info("========================================");

        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @DeleteMapping("/discard")
    public ResponseEntity<Map<String, Object>> discardChanges(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");

        log.info("========================================");
        log.info("[API] DELETE /memory/session/discard");
        log.info("[API] Conversation ID: {}", conversationId);

        sessionManager.discardChanges(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已放弃未保存的更改");

        log.info("[API] Changes discarded");
        log.info("========================================");

        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        log.info("[API] GET /memory/session/active");

        var sessions = sessionManager.getActiveSessions();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("activeSessions", sessions);
        result.put("count", sessions.size());

        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @DeleteMapping("/close")
    public ResponseEntity<Map<String, Object>> closeSession(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");

        log.info("========================================");
        log.info("[API] DELETE /memory/session/close");
        log.info("[API] Conversation ID: {}", conversationId);

        sessionManager.closeSession(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Session closed: " + conversationId);

        log.info("[API] Session closed");
        log.info("========================================");

        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }
}
