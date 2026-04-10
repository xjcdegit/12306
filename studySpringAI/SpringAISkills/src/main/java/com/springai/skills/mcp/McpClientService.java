package com.springai.skills.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springai.skills.skill.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * MCP 客户端服务 - 封装与外部 MCP Server 的交互
    private final SyncMcpToolCallbackProvider mcpToolProvider;
    private final SkillService skillService;
    private final ObjectMapper objectMapper;
 * 
    public McpClientService(ChatClient chatClient,
                           @Autowired(required = false) SyncMcpToolCallbackProvider mcpToolProvider,
                           SkillService skillService) {
 * 包括获取工具列表、调用工具等操作。</p>
        this.mcpToolProvider = mcpToolProvider;
        this.skillService = skillService;
        this.objectMapper = new ObjectMapper();
        if (mcpToolProvider != null) {
            log.info("McpClientService initialized with MCP tools");
        } else {
            log.warn("McpClientService initialized without MCP tools (MCP client disabled)");
        }
    }

    public boolean isMcpAvailable() {
        return mcpToolProvider != null;
    }

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

    public String callWithMcpTools(String userPrompt) {
        log.info("Calling MCP tools with prompt: {}", userPrompt);
        
        if (mcpToolProvider == null) {
            return chatClient.prompt(userPrompt)
                .call()
                .content();
        }
        
 * <h3>使用示例：</h3>
            .toolCallbacks(mcpToolProvider)
 * <pre>
 * &#64;Autowired
 * private McpClientService mcpClientService;
 * 
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
 * @author SpringAI Skills Framework
 * @see SyncMcpToolCallbackProvider
 */
@Service
    public SyncMcpToolCallbackProvider getMcpToolProvider() {
        return mcpToolProvider;
    }

public class McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientService.class);

    public String executeSkill(String skillName, String userRequest) {
        log.info("Executing skill '{}' with request: {}", skillName, userRequest);
        
        String skillPrompt = skillService.getSkillPrompt(skillName);
        if (skillPrompt == null) {
            log.warn("Skill '{}' not found", skillName);
            return "Skill '" + skillName + "' not found";
        }
        
        if (mcpToolProvider == null) {
            log.warn("MCP tools not available, executing without tools");
            return chatClient.prompt()
                .system(skillPrompt)
                .user(userRequest)
                .call()
                .content();
        }
        
        return chatClient.prompt()
            .system(skillPrompt)
            .user(userRequest)
            .toolCallbacks(mcpToolProvider)
            .call()
            .content();
    }

    public boolean hasSkill(String skillName) {
        return skillService.hasSkill(skillName);
    }

    public ChatResponse processWithSkillRouting(String userRequest) {
        log.info("Processing request with skill routing: {}", userRequest);
        
        String routerPrompt = skillService.getRouterPrompt();
        
        String routerResponse = chatClient.prompt()
            .system(routerPrompt)
            .user(userRequest)
            .call()
            .content();
        
        log.info("Router response: {}", routerResponse);
        
        String selectedSkill = parseSkillFromResponse(routerResponse);
        log.info("Selected skill: {}", selectedSkill);
        
        if ("none".equals(selectedSkill) || selectedSkill == null) {
            String result = callWithMcpTools(userRequest);
            return new ChatResponse(result, null, "No skill selected, direct response");
        }
        
        String skillPrompt = skillService.getSkillPrompt(selectedSkill);
        if (skillPrompt == null) {
            String result = callWithMcpTools(userRequest);
            return new ChatResponse(result, null, "Skill '" + selectedSkill + "' not found");
        }
        
        String result = executeSkill(selectedSkill, userRequest);
        return new ChatResponse(result, selectedSkill, "Executed skill: " + selectedSkill);
    }

    private String parseSkillFromResponse(String response) {
        try {
            String jsonStr = response;
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonStr = response.substring(jsonStart, jsonEnd + 1);
            }
            
            JsonNode node = objectMapper.readTree(jsonStr);
            if (node.has("skill")) {
                return node.get("skill").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to parse skill from response: {}", e.getMessage());
        }
        return null;
    }

    public static record ChatResponse(String result, String skill, String message) {}

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
