package com.springai.skills.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * MCP 客户端服务 - 封装与外部 MCP Server 的交互
 * 
 * <p>McpClientService 提供了与外部 MCP Server 通信的核心功能，
 * 包括获取工具列表、调用工具等操作。</p>
 * 
 * <h3>IoC 设计说明：</h3>
 * <p>该类使用 @Service 注解，由 Spring 容器自动创建和管理。
 * ChatClient.Builder 通过构造函数注入，SyncMcpToolCallbackProvider 为可选依赖。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * &#64;Autowired
 * private McpClientService mcpClientService;
 * 
 * public void demo() {
 *     String result = mcpClientService.callWithMcpTools("List files in workspace");
 * }
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see SyncMcpToolCallbackProvider
 */
@Service
public class McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientService.class);

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpToolProvider;

    public McpClientService(ChatClient.Builder chatClientBuilder,
                           @Autowired(required = false) SyncMcpToolCallbackProvider mcpToolProvider) {
        this.chatClient = chatClientBuilder.build();
        this.mcpToolProvider = mcpToolProvider;
        if (mcpToolProvider != null) {
            log.info("McpClientService initialized with MCP tools");
        } else {
            log.warn("McpClientService initialized without MCP tools (MCP client disabled)");
        }
    }

    /**
     * 检查 MCP 工具是否可用
     * 
     * @return true 如果 MCP 工具可用
     */
    public boolean isMcpAvailable() {
        return mcpToolProvider != null;
    }

    /**
     * 获取 MCP 工具提供者的工具数量
     * 
     * @return 工具数量
     */
    public int getToolCount() {
        if (mcpToolProvider == null) {
            return 0;
        }
        try {
            return mcpToolProvider.getToolCallbacks().length;
        } catch (Exception e) {
            log.warn("Failed to get tool count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 使用 MCP 工具执行用户提示
     * 
     * <p>将用户提示发送给 LLM，并自动使用 MCP Server 提供的工具。</p>
     * 
     * @param userPrompt 用户提示
     * @return LLM 响应内容
     */
    public String callWithMcpTools(String userPrompt) {
        log.info("Calling MCP tools with prompt: {}", userPrompt);
        
        if (mcpToolProvider == null) {
            return chatClient.prompt(userPrompt)
                .call()
                .content();
        }
        
        return chatClient.prompt(userPrompt)
            .toolCallbacks(mcpToolProvider)
            .call()
            .content();
    }

    /**
     * 使用 MCP 工具和自定义上下文执行用户提示
     * 
     * @param userPrompt 用户提示
     * @param toolContext 工具上下文（如进度令牌等）
     * @return LLM 响应内容
     */
    public String callWithMcpTools(String userPrompt, Map<String, Object> toolContext) {
        log.info("Calling MCP tools with prompt and context: {}", userPrompt);
        
        if (mcpToolProvider == null) {
            return chatClient.prompt(userPrompt)
                .call()
                .content();
        }
        
        return chatClient.prompt(userPrompt)
            .toolContext(toolContext)
            .toolCallbacks(mcpToolProvider)
            .call()
            .content();
    }

    /**
     * 获取 MCP 工具提供者
     * 
     * <p>返回 SyncMcpToolCallbackProvider，可用于手动注册到其他 ChatClient。</p>
     * 
     * @return MCP 工具提供者，可能为 null
     */
    public SyncMcpToolCallbackProvider getMcpToolProvider() {
        return mcpToolProvider;
    }

    /**
     * 获取 ChatClient 实例
     * 
     * @return ChatClient 实例
     */
    public ChatClient getChatClient() {
        return chatClient;
    }
}
