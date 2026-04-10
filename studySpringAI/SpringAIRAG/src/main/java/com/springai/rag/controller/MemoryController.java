package com.springai.rag.controller;

import com.springai.rag.agent.AgentEngine;
import com.springai.rag.memory.MemoryManager;
import com.springai.rag.memory.entity.ChatRecord;
import com.springai.rag.skill.SkillAwareAgentEngine;
import com.springai.rag.skill.SkillRouter.SkillInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/memory")
public class MemoryController {

    private static final Logger log = LoggerFactory.getLogger(MemoryController.class);

    private final AgentEngine agentEngine;
    private final SkillAwareAgentEngine skillAwareAgentEngine;
    private final MemoryManager memoryManager;

    public MemoryController(AgentEngine agentEngine, 
                           SkillAwareAgentEngine skillAwareAgentEngine,
                           MemoryManager memoryManager) {
        this.agentEngine = agentEngine;
        this.skillAwareAgentEngine = skillAwareAgentEngine;
        this.memoryManager = memoryManager;
    }

    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("userId") String userId,
            @RequestParam("message") String message) {
        
        log.info("========================================");
        log.info("[API] GET /memory/chat");
        log.info("[API] 会话ID: {}", conversationId);
        log.info("[API] 用户ID: {}", userId);
        log.info("[API] 消息: {}", message);
        
        String response = skillAwareAgentEngine.process(conversationId, userId, message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conversationId);
        result.put("userId", userId);
        result.put("response", response);
        
        log.info("[API] 响应成功，长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chat-simple")
    public ResponseEntity<Map<String, Object>> chatSimple(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("userId") String userId,
            @RequestParam("message") String message) {
        
        log.info("========================================");
        log.info("[API] GET /memory/chat-simple");
        log.info("[API] 会话ID: {}", conversationId);
        log.info("[API] 用户ID: {}", userId);
        log.info("[API] 消息: {}", message);
        
        String response = agentEngine.process(conversationId, userId, message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conversationId);
        result.put("userId", userId);
        result.put("response", response);
        
        log.info("[API] 响应成功，长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/skill-match")
    public ResponseEntity<Map<String, Object>> getMatchedSkill(
            @RequestParam("message") String message) {
        
        log.info("========================================");
        log.info("[API] GET /memory/skill-match");
        log.info("[API] 消息: {}", message);
        
        SkillInfo matchedSkill = skillAwareAgentEngine.getMatchedSkill(message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        
        if (matchedSkill != null) {
            result.put("skillMatched", true);
            result.put("skillName", matchedSkill.getName());
            result.put("skillDescription", matchedSkill.getDescription());
            result.put("mcpServerType", matchedSkill.getMcpServerType());
            log.info("[API] 匹配到Skill: {}", matchedSkill.getName());
        } else {
            result.put("skillMatched", false);
            result.put("skillName", null);
            log.info("[API] 未匹配到Skill");
        }
        
        log.info("========================================");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentMessages(
            @RequestParam("conversationId") String conversationId,
            @RequestParam(value = "lastN", defaultValue = "10") int lastN) {
        
        log.info("[API] GET /memory/recent - 会话: {}, 数量: {}", conversationId, lastN);
        
        var messages = memoryManager.getRecentMessages(conversationId, lastN);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conversationId);
        result.put("count", messages.size());
        result.put("messages", messages);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteHistory(
            @RequestParam("conversationId") String conversationId) {
        
        log.info("[API] GET /memory/complete - 会话: {}", conversationId);
        
        List<ChatRecord> history = memoryManager.getCompleteHistory(conversationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conversationId);
        result.put("count", history.size());
        result.put("history", history);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-history")
    public ResponseEntity<Map<String, Object>> getUserHistory(
            @RequestParam("userId") String userId,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        
        log.info("[API] GET /memory/user-history - 用户: {}, 限制: {}", userId, limit);
        
        List<ChatRecord> history = memoryManager.getUserHistory(userId, limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("count", history.size());
        result.put("history", history);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("userId") String userId) {
        
        log.info("[API] GET /memory/stats - 会话: {}, 用户: {}", conversationId, userId);
        
        MemoryManager.MemoryStats stats = memoryManager.getMemoryStats(conversationId, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conversationId);
        result.put("userId", userId);
        result.put("stats", stats.toMap());
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Map<String, Object>> clearConversation(
            @PathVariable("conversationId") String conversationId) {
        
        log.info("[API] DELETE /memory/conversation/{}", conversationId);
        
        skillAwareAgentEngine.clearConversation(conversationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Conversation cleared: " + conversationId);
        
        return ResponseEntity.ok(result);
    }
}
