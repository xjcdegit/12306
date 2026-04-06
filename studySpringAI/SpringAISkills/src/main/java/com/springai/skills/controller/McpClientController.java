package com.springai.skills.controller;

import com.springai.skills.mcp.McpClientService;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 客户端控制器 - MCP 工具调用 REST API
 * 
 * <p>McpClientController 提供了通过 MCP 协议调用外部工具的 REST API 端点，
 * 允许 AI 应用与外部 MCP Server 进行交互。</p>
 * 
 * <h3>API 端点列表：</h3>
 * <pre>
 * GET  /api/mcp/status         - 获取 MCP 状态
 * GET  /api/mcp/tools          - 获取所有 MCP 工具列表
 * GET  /api/mcp/tools/count    - 获取 MCP 工具数量
 * GET  /api/mcp/chat           - 使用 MCP 工具进行对话 (参数: message)
 * POST /api/mcp/chat-with-context - 带上下文的 MCP 对话
 * </pre>
 * 
 * @author SpringAI Skills Framework
 */
@RestController
@RequestMapping("/api/mcp")
public class McpClientController {

    private final McpClientService mcpClientService;

    public McpClientController(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    /**
     * 获取 MCP 状态
     * 
     * @return MCP 状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mcpAvailable", mcpClientService.isMcpAvailable());
        status.put("toolCount", mcpClientService.getToolCount());
        return ResponseEntity.ok(status);
    }

    /**
     * 获取 MCP 工具数量
     * 
     * @return 工具数量
     */
    @GetMapping("/tools/count")
    public ResponseEntity<Map<String, Integer>> getToolCount() {
        return ResponseEntity.ok(Map.of("count", mcpClientService.getToolCount()));
    }

    /**
     * 获取所有 MCP 工具列表
     * 
     * @return 工具列表
     */
    @GetMapping("/tools")
    public ResponseEntity<?> getAllTools() {
        if (!mcpClientService.isMcpAvailable()) {
            return ResponseEntity.ok(Map.of("message", "MCP client is disabled"));
        }
        
        List<Map<String, Object>> tools = new ArrayList<>();
        try {
            SyncMcpToolCallbackProvider provider = mcpClientService.getMcpToolProvider();
            if (provider != null) {
                ToolCallback[] callbacks = provider.getToolCallbacks();
                for (ToolCallback callback : callbacks) {
                    Map<String, Object> toolInfo = new HashMap<>();
                    toolInfo.put("name", callback.getToolDefinition().name());
                    toolInfo.put("description", callback.getToolDefinition().description());
                    tools.add(toolInfo);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.ok(List.of(Map.of("error", e.getMessage())));
        }
        return ResponseEntity.ok(tools);
    }

    /**
     * 使用 MCP 工具进行对话
     * 
     * @param message 用户提示词
     * @return LLM 响应
     */
    @GetMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestParam("message") String message) {
        String result = mcpClientService.callWithMcpTools(message);
        return ResponseEntity.ok(Map.of("result", result));
    }

    /**
     * 使用 MCP 工具和上下文进行对话
     * 
     * @param request 包含 prompt 和 context 的请求体
     * @return LLM 响应
     */
    @PostMapping("/chat-with-context")
    public ResponseEntity<Map<String, String>> chatWithContext(@RequestBody McpChatWithContextRequest request) {
        String result = mcpClientService.callWithMcpTools(request.prompt(), request.context());
        return ResponseEntity.ok(Map.of("result", result));
    }

    public record McpChatWithContextRequest(String prompt, Map<String, Object> context) {}
}
