package com.springai.rag.controller;

import com.springai.rag.mcp.McpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    private final McpClientService mcpClientService;

    public McpController(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getAvailableTools() {
        log.info("========================================");
        log.info("[API] GET /api/mcp/tools");
        
        List<Map<String, Object>> tools = mcpClientService.getAvailableTools();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", tools.size());
        result.put("tools", tools);
        result.put("mcpEnabled", mcpClientService.hasMcpTools());
        
        log.info("[API] 返回 {} 个工具", tools.size());
        log.info("========================================");
        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestParam("prompt") String prompt) {
        log.info("========================================");
        log.info("[API] GET /api/mcp/chat");
        log.info("[API] 提示词: {}", prompt);
        
        String response = mcpClientService.chat(prompt);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("response", response);
        
        log.info("[API] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @GetMapping("/chat-with-context")
    public ResponseEntity<Map<String, Object>> chatWithContext(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "userId", required = false) String userId) {
        
        log.info("========================================");
        log.info("[API] GET /api/mcp/chat-with-context");
        log.info("[API] 提示词: {}", prompt);
        log.info("[API] 会话ID: {}", conversationId);
        log.info("[API] 用户ID: {}", userId);
        
        Map<String, Object> context = new HashMap<>();
        if (conversationId != null) context.put("conversationId", conversationId);
        if (userId != null) context.put("userId", userId);
        
        String response = mcpClientService.callWithMcpTools(prompt, context);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("response", response);
        
        log.info("[API] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("[API] GET /api/mcp/status");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("mcpEnabled", mcpClientService.hasMcpTools());
        result.put("toolCount", mcpClientService.getAvailableTools().size());
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(result);
    }
}
