package com.springai.rag.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientService.class);

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpToolProvider;

    @Autowired
    public McpClientService(ChatClient.Builder chatClientBuilder,
                           @Autowired(required = false) SyncMcpToolCallbackProvider mcpToolProvider) {
        this.chatClient = chatClientBuilder.build();
        this.mcpToolProvider = mcpToolProvider;
        log.info("========================================");
        log.info("[MCP] McpClientService 初始化");
        log.info("[MCP] MCP工具提供者: {}", mcpToolProvider != null ? "已加载" : "未找到");
        log.info("========================================");
    }

    public String chat(String userPrompt) {
        log.info("========================================");
        log.info("[MCP-CHAT] 开始聊天");
        log.info("[MCP-CHAT] 用户提示词: {}", userPrompt);
        log.info("[MCP-CHAT] MCP工具: {}", mcpToolProvider != null ? "可用" : "不可用");
        
        String response;
        if (mcpToolProvider != null) {
            log.info("[MCP-CHAT] 使用MCP工具进行对话...");
            response = chatClient.prompt(userPrompt)
                .toolCallbacks(mcpToolProvider)
                .call()
                .content();
        } else {
            log.info("[MCP-CHAT] 无MCP工具，使用普通对话...");
            response = chatClient.prompt(userPrompt)
                .call()
                .content();
        }
        
        log.info("[MCP-CHAT] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    public String chatWithSystem(String systemPrompt, String userPrompt) {
        log.info("========================================");
        log.info("[MCP-SYSTEM] 开始系统对话");
        log.info("[MCP-SYSTEM] 用户提示词: {}", userPrompt);
        log.info("[MCP-SYSTEM] 系统提示词长度: {} 字符", systemPrompt.length());
        log.info("[MCP-SYSTEM] MCP工具: {}", mcpToolProvider != null ? "可用" : "不可用");
        
        String response;
        if (mcpToolProvider != null) {
            log.info("[MCP-SYSTEM] 调用ChatClient，启用MCP工具...");
            response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .toolCallbacks(mcpToolProvider)
                .call()
                .content();
        } else {
            log.info("[MCP-SYSTEM] 调用ChatClient，无MCP工具...");
            response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
        }
        
        log.info("[MCP-SYSTEM] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    public String callWithMcpTools(String userPrompt, Map<String, Object> context) {
        log.info("========================================");
        log.info("[MCP-TOOLS] 开始调用MCP工具");
        log.info("[MCP-TOOLS] 用户提示词: {}", userPrompt);
        log.info("[MCP-TOOLS] 上下文: {}", context);
        
        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append("你是一个智能助手，可以使用MCP工具来完成用户请求。\n\n");
        
        if (context != null && !context.isEmpty()) {
            systemPromptBuilder.append("上下文信息：\n");
            context.forEach((key, value) -> 
                systemPromptBuilder.append(String.format("- %s: %s\n", key, value)));
        }
        
        String response;
        if (mcpToolProvider != null) {
            log.info("[MCP-TOOLS] 使用MCP工具调用...");
            response = chatClient.prompt()
                .system(systemPromptBuilder.toString())
                .user(userPrompt)
                .toolCallbacks(mcpToolProvider)
                .call()
                .content();
        } else {
            log.info("[MCP-TOOLS] 无MCP工具，普通调用...");
            response = chatClient.prompt()
                .system(systemPromptBuilder.toString())
                .user(userPrompt)
                .call()
                .content();
        }
        
        log.info("[MCP-TOOLS] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    public List<Map<String, Object>> getAvailableTools() {
        log.info("========================================");
        log.info("[MCP-TOOLS-LIST] 获取可用工具列表");
        
        List<Map<String, Object>> tools = new ArrayList<>();
        
        if (mcpToolProvider != null) {
            ToolCallback[] toolCallbacks = mcpToolProvider.getToolCallbacks();
            log.info("[MCP-TOOLS-LIST] 发现 {} 个工具", toolCallbacks.length);
            
            for (int i = 0; i < toolCallbacks.length; i++) {
                ToolCallback callback = toolCallbacks[i];
                String name = callback.getToolDefinition().name();
                String description = callback.getToolDefinition().description();
                
                Map<String, Object> toolInfo = new HashMap<>();
                toolInfo.put("name", name);
                toolInfo.put("description", description);
                tools.add(toolInfo);
                
                log.info("[MCP-TOOLS-LIST] 工具 #{}: {}", i + 1, name);
                log.info("[MCP-TOOLS-LIST]   描述: {}", description);
            }
        } else {
            log.warn("[MCP-TOOLS-LIST] MCP工具提供者不可用");
        }
        
        log.info("[MCP-TOOLS-LIST] 总计: {} 个工具", tools.size());
        log.info("========================================");
        return tools;
    }

    public boolean hasMcpTools() {
        return mcpToolProvider != null;
    }

    public ChatClient getChatClient() {
        return chatClient;
    }
}
