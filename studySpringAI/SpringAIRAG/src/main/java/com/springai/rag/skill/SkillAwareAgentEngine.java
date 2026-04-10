package com.springai.rag.skill;

import com.springai.rag.memory.MemoryManager;
import com.springai.rag.mcp.McpClientService;
import com.springai.rag.skill.SkillRouter.SkillInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SkillAwareAgentEngine {

    private static final Logger log = LoggerFactory.getLogger(SkillAwareAgentEngine.class);

    private final MemoryManager memoryManager;
    private final McpClientService mcpClientService;
    private final SkillRouter skillRouter;
    private final ChatClient chatClient;

    public SkillAwareAgentEngine(MemoryManager memoryManager, 
                                  McpClientService mcpClientService,
                                  SkillRouter skillRouter,
                                  ChatClient.Builder chatClientBuilder) {
        this.memoryManager = memoryManager;
        this.mcpClientService = mcpClientService;
        this.skillRouter = skillRouter;
        this.chatClient = chatClientBuilder.build();
        log.info("========================================");
        log.info("[AGENT] SkillAwareAgentEngine 初始化完成");
        log.info("========================================");
    }

    public String process(String conversationId, String userId, String userMessage) {
        log.info("========================================");
        log.info("[AGENT] ========== 开始处理消息 ==========");
        log.info("[AGENT] 会话ID: {}", conversationId);
        log.info("[AGENT] 用户ID: {}", userId);
        log.info("[AGENT] 用户消息: {}", userMessage);
        log.info("========================================");
        
        log.info("[AGENT] 步骤1: 保存用户消息到记忆...");
        memoryManager.addUserMessage(conversationId, userId, userMessage);
        log.info("[AGENT] 用户消息已保存");
        
        log.info("[AGENT] 步骤2: 获取对话上下文...");
        List<Message> context = memoryManager.getContextForConversation(conversationId, userId, 10);
        log.info("[AGENT] 加载了 {} 条历史消息", context.size());
        
        log.info("[AGENT] 步骤3: 路由到合适的Skill...");
        SkillInfo matchedSkill = skillRouter.routeSkill(userMessage);
        
        String response;
        if (matchedSkill != null) {
            log.info("[AGENT] ✓ 匹配到Skill: {}", matchedSkill.getName());
            log.info("[AGENT]   Skill描述: {}", matchedSkill.getDescription());
            log.info("[AGENT]   MCP类型: {}", matchedSkill.getMcpServerType());
            log.info("[AGENT] 步骤4: 使用Skill处理...");
            response = processWithSkill(matchedSkill, userMessage, context, conversationId, userId);
        } else {
            log.info("[AGENT] ✗ 未匹配到特定Skill");
            log.info("[AGENT] 步骤4: 使用默认处理...");
            response = processDefault(userMessage, context, conversationId, userId);
        }
        
        log.info("[AGENT] 步骤5: 保存助手回复到记忆...");
        memoryManager.addAssistantMessage(conversationId, userId, response);
        log.info("[AGENT] 助手回复已保存");
        
        log.info("========================================");
        log.info("[AGENT] ========== 处理完成 ==========");
        log.info("[AGENT] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    private String processWithSkill(SkillInfo skill, String userMessage, 
                                     List<Message> context, String conversationId, String userId) {
        log.info("========================================");
        log.info("[AGENT-SKILL] 使用Skill处理消息");
        log.info("[AGENT-SKILL] Skill名称: {}", skill.getName());
        
        log.info("[AGENT-SKILL] 构建系统提示词...");
        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append("你是一个智能助手。请根据以下技能指南来回答用户问题：\n\n");
        systemPromptBuilder.append("=== 技能指南 ===\n");
        systemPromptBuilder.append(skill.getContent());
        systemPromptBuilder.append("\n=== 技能指南结束 ===\n\n");
        
        systemPromptBuilder.append("对话上下文：\n");
        systemPromptBuilder.append("- 会话ID: ").append(conversationId).append("\n");
        systemPromptBuilder.append("- 用户ID: ").append(userId).append("\n");
        systemPromptBuilder.append("- 历史消息数: ").append(context.size()).append("\n\n");
        
        systemPromptBuilder.append("你可以使用以下工具来完成任务：\n");
        systemPromptBuilder.append("- 高德地图工具：用于地理编码、路径规划、POI搜索等\n");
        systemPromptBuilder.append("- 记忆工具：用于保存和检索对话历史\n\n");
        
        systemPromptBuilder.append("请根据技能指南，使用合适的工具来回答用户问题。");
        
        log.info("[AGENT-SKILL] 系统提示词长度: {} 字符", systemPromptBuilder.length());
        log.info("[AGENT-SKILL] 调用MCP服务...");
        
        String response = mcpClientService.chatWithSystem(systemPromptBuilder.toString(), userMessage);
        
        log.info("[AGENT-SKILL] MCP响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    private String processDefault(String userMessage, List<Message> context, 
                                   String conversationId, String userId) {
        log.info("========================================");
        log.info("[AGENT-DEFAULT] 使用默认处理");
        
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("conversationId", conversationId);
        contextMap.put("userId", userId);
        contextMap.put("historyCount", context.size());
        
        log.info("[AGENT-DEFAULT] 上下文: {}", contextMap);
        log.info("[AGENT-DEFAULT] 调用MCP服务...");
        
        String response = mcpClientService.callWithMcpTools(userMessage, contextMap);
        
        log.info("[AGENT-DEFAULT] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    public SkillInfo getMatchedSkill(String message) {
        log.info("[AGENT] 获取匹配的Skill: {}", message);
        return skillRouter.routeSkill(message);
    }

    public void clearConversation(String conversationId) {
        log.info("[AGENT] 清除会话: {}", conversationId);
        memoryManager.clearConversation(conversationId);
    }
}
