package com.springai.rag.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MemoryCompressor {

    private static final Logger log = LoggerFactory.getLogger(MemoryCompressor.class);

    private static final String COMPRESSION_SYSTEM_PROMPT = """
        你是一个专业的对话摘要助手。请将以下对话历史压缩为简洁的结构化摘要。
        
        压缩要求：
        1. 保留用户的核心需求和意图
        2. 记录已确定的重要信息（地点、时间、偏好等）
        3. 列出已推荐的内容（景点、美食、方案等）
        4. 标记未解决的问题或待办事项
        5. 保持时间线和因果关系
        
        请按以下格式输出：
        
        ## 用户意图
        - [核心需求描述]
        
        ## 已确定信息
        - 地点: [具体地点]
        - 时间: [具体时间]
        - 偏好: [用户偏好]
        
        ## 推荐内容
        - 景点: [已推荐景点列表]
        - 美食: [已推荐美食列表]
        - 其他: [其他推荐]
        
        ## 待办事项
        - [未完成的任务]
        
        ## 重要上下文
        - [其他需要记住的关键信息]
        """;

    private final ChatClient chatClient;

    public MemoryCompressor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        log.info("[COMPRESSOR] MemoryCompressor initialized");
    }

    public String compress(List<Message> messages) {
        log.info("========================================");
        log.info("[COMPRESSOR] Starting compression");
        log.info("[COMPRESSOR] Input messages: {}", messages.size());

        if (messages == null || messages.isEmpty()) {
            log.info("[COMPRESSOR] No messages to compress");
            return "";
        }

        String conversationText = formatMessages(messages);
        log.info("[COMPRESSOR] Conversation text length: {} characters", conversationText.length());

        try {
            String compressed = chatClient.prompt()
                .system(COMPRESSION_SYSTEM_PROMPT)
                .user("请压缩以下对话历史：\n\n" + conversationText)
                .call()
                .content();

            log.info("[COMPRESSOR] Compression complete. Output length: {} characters", 
                compressed != null ? compressed.length() : 0);
            log.info("========================================");
            
            return compressed != null ? compressed : "";
        } catch (Exception e) {
            log.error("[COMPRESSOR] Compression failed", e);
            return fallbackCompress(messages);
        }
    }

    public String compressWithTemplate(List<Message> messages, String customTemplate) {
        log.info("[COMPRESSOR] Compressing with custom template");
        
        String conversationText = formatMessages(messages);
        
        try {
            return chatClient.prompt()
                .system(customTemplate)
                .user(conversationText)
                .call()
                .content();
        } catch (Exception e) {
            log.error("[COMPRESSOR] Custom compression failed", e);
            return fallbackCompress(messages);
        }
    }

    public int estimateTokens(String content) {
        if (content == null) return 0;
        return content.length() / 4;
    }

    public int estimateTokens(List<Message> messages) {
        if (messages == null) return 0;
        return messages.stream()
            .mapToInt(m -> estimateTokens(m.getText()))
            .sum();
    }

    private String formatMessages(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        
        for (Message msg : messages) {
            String role = msg.getMessageType().getValue();
            String content = msg.getText();
            
            sb.append("[").append(role.toUpperCase()).append("]\n");
            sb.append(content).append("\n\n");
        }
        
        return sb.toString();
    }

    private String fallbackCompress(List<Message> messages) {
        log.info("[COMPRESSOR] Using fallback compression");
        
        StringBuilder summary = new StringBuilder();
        summary.append("## 会话摘要\n\n");
        
        int userCount = 0;
        int assistantCount = 0;
        
        for (Message msg : messages) {
            String role = msg.getMessageType().getValue();
            if ("user".equals(role)) userCount++;
            else if ("assistant".equals(role)) assistantCount++;
        }
        
        summary.append("- 对话轮数: ").append(Math.min(userCount, assistantCount)).append("\n");
        summary.append("- 总消息数: ").append(messages.size()).append("\n");
        
        if (!messages.isEmpty()) {
            Message lastUser = null;
            for (int i = messages.size() - 1; i >= 0; i--) {
                if ("user".equals(messages.get(i).getMessageType().getValue())) {
                    lastUser = messages.get(i);
                    break;
                }
            }
            if (lastUser != null) {
                summary.append("- 最后用户消息: ").append(truncate(lastUser.getText(), 100)).append("\n");
            }
        }
        
        return summary.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
