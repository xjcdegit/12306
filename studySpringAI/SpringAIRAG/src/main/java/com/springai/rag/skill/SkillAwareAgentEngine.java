package com.springai.rag.skill;

import com.springai.rag.memory.SessionManager;
import com.springai.rag.memory.SessionManager.SessionStatus;
import com.springai.rag.mcp.McpClientService;
import com.springai.rag.skill.SkillRouter.SkillInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillAwareAgentEngine {

    private static final Logger log = LoggerFactory.getLogger(SkillAwareAgentEngine.class);

    private static final int CONTEXT_WINDOW = 10;

    private final SessionManager sessionManager;
    private final McpClientService mcpClientService;
    private final SkillRouter skillRouter;
    private final ChatClient chatClient;

    public SkillAwareAgentEngine(SessionManager sessionManager, 
                                  McpClientService mcpClientService,
                                  SkillRouter skillRouter,
                                  ChatClient.Builder chatClientBuilder) {
        this.sessionManager = sessionManager;
        this.mcpClientService = mcpClientService;
        this.skillRouter = skillRouter;
        this.chatClient = chatClientBuilder.build();
        log.info("========================================");
        log.info("[AGENT] SkillAwareAgentEngine 初始化完成");
        log.info("[AGENT] 使用 SessionManager 管理短期记忆");
        log.info("========================================");
    }

    public String process(String conversationId, String userId, String userMessage) {
        log.info("========================================");
        log.info("[AGENT] ========== 开始处理消息 ==========");
        log.info("[AGENT] 会话ID: {}", conversationId);
        log.info("[AGENT] 用户ID: {}", userId);
        log.info("[AGENT] 用户消息: {}", userMessage);
        log.info("========================================");

        SessionStatus status = sessionManager.getSessionStatus(conversationId);
        if (!status.isCanChat()) {
            log.warn("[AGENT] 会话已达到记忆上限，需要保存");
            return "记忆已达上限，请先保存当前对话后再继续。点击保存按钮即可压缩并保存对话历史。";
        }
        
        log.info("[AGENT] 步骤1: 保存用户消息到短期记忆...");
        sessionManager.addUserMessage(conversationId, userMessage);
        log.info("[AGENT] 用户消息已保存到短期记忆");
        
        log.info("[AGENT] 步骤2: 从短期记忆获取对话上下文...");
        List<Message> context = sessionManager.getContext(conversationId, CONTEXT_WINDOW);
        log.info("[AGENT] 从短期记忆加载了 {} 条历史消息", context.size());
        
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
        
        log.info("[AGENT] 步骤5: 保存助手回复到短期记忆...");
        sessionManager.addAssistantMessage(conversationId, response);
        log.info("[AGENT] 助手回复已保存到短期记忆");

        SessionStatus newStatus = sessionManager.getSessionStatus(conversationId);
        if (newStatus.getWarning() != null) {
            log.info("[AGENT] 警告: {}", newStatus.getWarning());
        }
        
        log.info("========================================");
        log.info("[AGENT] ========== 处理完成 ==========");
        log.info("[AGENT] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("[AGENT] 当前短期记忆: {} 条消息", newStatus.getShortTermCount());
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
        systemPromptBuilder.append("- 高德地图工具：用于地理编码、路径规划、POI搜索等\n\n");
        
        systemPromptBuilder.append("请根据技能指南，使用合适的工具来回答用户问题。");
        
        log.info("[AGENT-SKILL] 系统提示词长度: {} 字符", systemPromptBuilder.length());
        log.info("[AGENT-SKILL] 调用MCP服务 (带历史上下文)...");
        
        String response = mcpClientService.chatWithHistory(
            systemPromptBuilder.toString(), 
            userMessage, 
            context
        );
        
        log.info("[AGENT-SKILL] MCP响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    private String processDefault(String userMessage, List<Message> context, 
                                   String conversationId, String userId) {
        log.info("========================================");
        log.info("[AGENT-DEFAULT] 使用默认处理");
        log.info("[AGENT-DEFAULT] 历史消息数: {}", context.size());

        String systemPrompt = "你是一个友好的AI助手。请根据对话历史和用户当前的问题，给出有帮助的回答。";
        
        log.info("[AGENT-DEFAULT] 调用MCP服务 (带历史上下文)...");
        
        String response = mcpClientService.chatWithHistory(systemPrompt, userMessage, context);
        
        log.info("[AGENT-DEFAULT] 响应长度: {} 字符", response != null ? response.length() : 0);
        log.info("========================================");
        return response;
    }

    public SkillInfo getMatchedSkill(String message) {
        log.info("[AGENT] 获取匹配的Skill: {}", message);
        return skillRouter.routeSkill(message);
    }

    public SessionStatus getSessionStatus(String conversationId) {
        return sessionManager.getSessionStatus(conversationId);
    }

    public void clearConversation(String conversationId) {
        log.info("[AGENT] 清除会话: {}", conversationId);
        sessionManager.closeSession(conversationId);
    }
}
