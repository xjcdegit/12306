package com.springai.rag.agent;

import com.springai.rag.memory.MemoryManager;
import com.springai.rag.mcp.McpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentEngine {

    private static final Logger log = LoggerFactory.getLogger(AgentEngine.class);

    private final MemoryManager memoryManager;
    private final McpClientService mcpClientService;

    private static final String SYSTEM_PROMPT = """
        你是一个智能助手，具备以下能力：
        1. 记忆上下文：能够记住对话历史
        2. 工具调用：可以使用MCP工具完成复杂任务
        3. 智能回答：根据上下文和工具结果生成准确回复
        
        请根据用户的问题，选择合适的方式回答。
        """;

    public AgentEngine(MemoryManager memoryManager, McpClientService mcpClientService) {
        this.memoryManager = memoryManager;
        this.mcpClientService = mcpClientService;
        log.info("AgentEngine initialized");
    }

    public String process(String conversationId, String userId, String userMessage) {
        log.info("Processing message for conversation: {}, user: {}", conversationId, userId);
        
        memoryManager.addUserMessage(conversationId, userId, userMessage);
        
        List<Message> context = memoryManager.getContextForConversation(conversationId, userId, 10);
        log.debug("Loaded {} messages from memory", context.size());
        
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("conversationId", conversationId);
        contextMap.put("userId", userId);
        contextMap.put("historyCount", context.size());
        
        String response = mcpClientService.callWithMcpTools(userMessage, contextMap);
        
        memoryManager.addAssistantMessage(conversationId, userId, response);
        
        log.info("Response generated for conversation: {}", conversationId);
        return response;
    }

    public String processWithSystemPrompt(String conversationId, String userId, 
                                          String systemPrompt, String userMessage) {
        log.info("Processing with custom system prompt for conversation: {}", conversationId);
        
        memoryManager.addUserMessage(conversationId, userId, userMessage);
        
        String response = mcpClientService.chatWithSystem(systemPrompt, userMessage);
        
        memoryManager.addAssistantMessage(conversationId, userId, response);
        
        return response;
    }

    public void addToolResult(String conversationId, String userId, String toolResult) {
        memoryManager.addToolMessage(conversationId, userId, toolResult);
        log.debug("Added tool result to conversation: {}", conversationId);
    }

    public void clearConversation(String conversationId) {
        memoryManager.clearConversation(conversationId);
        log.info("Cleared conversation: {}", conversationId);
    }

    public MemoryManager.MemoryStats getStats(String conversationId, String userId) {
        return memoryManager.getMemoryStats(conversationId, userId);
    }

    public List<Message> getConversationContext(String conversationId, String userId) {
        return memoryManager.getContextForConversation(conversationId, userId, 20);
    }
}
