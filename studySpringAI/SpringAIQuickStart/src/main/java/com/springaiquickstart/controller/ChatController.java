package com.springaiquickstart.controller;

import com.springaiquickstart.rag.RagService;
import com.springaiquickstart.tools.ToolManager;
import com.springaiquickstart.mcp.ToolRegistry;
import com.springaiquickstart.mcp.ToolDispatcher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 * 
 * 提供AI聊天功能和智能工具调用接口
 * 使用Spring AI的Function Calling机制，让大模型自动选择调用哪个工具
 * 集成RAG检索增强生成功能
 * 集成MCP（Model Context Protocol）工具管理和调度
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient.Builder chatClientBuilder;
    private final ToolManager toolManager;
    private final RagService ragService;
    private final ToolRegistry toolRegistry;
    private final ToolDispatcher toolDispatcher;

    @Autowired
    public ChatController(ChatClient.Builder chatClientBuilder, ToolManager toolManager, RagService ragService, 
                         ToolRegistry toolRegistry, ToolDispatcher toolDispatcher) {
        this.chatClientBuilder = chatClientBuilder;
        this.toolManager = toolManager;
        this.ragService = ragService;
        this.toolRegistry = toolRegistry;
        this.toolDispatcher = toolDispatcher;
    }

    /**
     * 普通聊天接口
     * 不使用工具调用
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClientBuilder.build()
                .prompt(message)
                .call()
                .content();
    }

    /**
     * 智能工具调用接口
     * 让大模型自动选择调用哪个工具
     * 注意：这里简化实现，实际使用时需要配置Function Calling
     */
    @GetMapping("/smart-chat")
    public String smartChat(@RequestParam String message) {
        // 简化实现：直接返回聊天结果
        // 实际使用时需要配置Function Calling
        return chatClientBuilder.build()
                .prompt(message)
                .call()
                .content();
    }

    /**
     * RAG增强聊天接口
     * 结合知识库检索和工具调用
     */
    @GetMapping("/rag-chat")
    public String ragChat(@RequestParam String message) {
        // 1. 先检索知识库
        Map<String, Object> ragResult = ragService.query(message, 3);
        String context = (String) ragResult.get("context");
        
        // 2. 构建增强Prompt
        String enhancedPrompt = String.format(
            "上下文信息：\n%s\n\n用户问题：%s\n\n请基于上下文信息回答问题，如果上下文信息不足，请直接回答。",
            context, message
        );
        
        // 3. 返回聊天结果
        return chatClientBuilder.build()
                .prompt(enhancedPrompt)
                .call()
                .content();
    }

    /**
     * 流式聊天接口
     */
    @GetMapping("/stream-chat")
    public Flux<String> streamChat(@RequestParam String message) {
        return chatClientBuilder.build()
                .prompt(message)
                .stream()
                .content();
    }

    /**
     * MCP工具列表接口
     * 返回所有可用工具的定义
     */
    @GetMapping("/tools")
    public Map<String, Object> getTools() {
        return toolRegistry.getToolDefinitions();
    }

    /**
     * MCP工具推荐接口
     * 根据查询推荐合适的工具
     */
    @GetMapping("/recommend-tools")
    public List<Map<String, Object>> recommendTools(@RequestParam String query) {
        List<com.springaiquickstart.mcp.Tool> tools = toolDispatcher.recommendTools(query);
        List<Map<String, Object>> result = new ArrayList<>();
        for (com.springaiquickstart.mcp.Tool tool : tools) {
            result.add(Map.of(
                "name", tool.getName(),
                "description", tool.getDescription()
            ));
        }
        return result;
    }
}
