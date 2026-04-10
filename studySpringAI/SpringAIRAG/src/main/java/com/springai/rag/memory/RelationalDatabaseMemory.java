package com.springai.rag.memory;

import com.springai.rag.memory.entity.ChatRecord;
import com.springai.rag.memory.repository.ChatHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class RelationalDatabaseMemory {

    private static final Logger log = LoggerFactory.getLogger(RelationalDatabaseMemory.class);

    private final ChatHistoryRepository repository;
    private final ChatClient chatClient;

    @Value("${memory.compression.enabled:true}")
    private boolean compressionEnabled;

    @Value("${memory.compression.token-threshold:8000}")
    private int tokenThreshold;

    @Value("${memory.compression.keep-recent:10}")
    private int keepRecentMessages;

    public RelationalDatabaseMemory(ChatHistoryRepository repository, ChatClient.Builder chatClientBuilder) {
        this.repository = repository;
        this.chatClient = chatClientBuilder.build();
    }

    @Transactional
    public void addUserMessage(String conversationId, String userId, String content) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("user");
        record.setContent(content);
        record.setTokenCount(estimateTokenCount(content));
        record.setIsCompressed(false);
        repository.save(record);
        log.debug("Saved user message to database for conversation: {}", conversationId);
        
        checkAndCompress(conversationId);
    }

    @Transactional
    public void addAssistantMessage(String conversationId, String userId, String content) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("assistant");
        record.setContent(content);
        record.setTokenCount(estimateTokenCount(content));
        record.setIsCompressed(false);
        repository.save(record);
        log.debug("Saved assistant message to database for conversation: {}", conversationId);
        
        checkAndCompress(conversationId);
    }

    @Transactional
    public void addToolMessage(String conversationId, String userId, String content) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("tool");
        record.setContent(content);
        record.setTokenCount(estimateTokenCount(content));
        record.setIsCompressed(false);
        repository.save(record);
        log.debug("Saved tool message to database for conversation: {}", conversationId);
        
        checkAndCompress(conversationId);
    }

    @Transactional(readOnly = true)
    public List<ChatRecord> getCompleteHistory(String conversationId) {
        List<ChatRecord> compressedSummaries = repository.findCompressedSummaries(conversationId);
        List<ChatRecord> recentMessages = repository.findAllUncompressed(conversationId);
        
        List<ChatRecord> allRecords = new ArrayList<>();
        allRecords.addAll(compressedSummaries);
        allRecords.addAll(recentMessages);
        
        return allRecords;
    }

    @Transactional(readOnly = true)
    public List<ChatRecord> getUserHistory(String userId, int limit) {
        return repository.findUserHistory(userId, limit);
    }

    @Transactional
    public void clearConversation(String conversationId) {
        repository.deleteByConversationId(conversationId);
        log.info("Cleared conversation: {}", conversationId);
    }

    @Transactional(readOnly = true)
    public long getMessageCount(String conversationId) {
        return repository.countByConversationId(conversationId);
    }

    @Transactional(readOnly = true)
    public long getUserMessageCount(String userId) {
        return repository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public int getTotalTokenCount(String conversationId) {
        return repository.getTotalTokenCount(conversationId);
    }

    @Async
    @Transactional
    protected void checkAndCompress(String conversationId) {
        if (!compressionEnabled) {
            return;
        }
        
        int totalTokens = repository.getTotalTokenCount(conversationId);
        
        if (totalTokens > tokenThreshold) {
            log.info("Token count {} exceeds threshold {}, triggering compression for conversation: {}", 
                totalTokens, tokenThreshold, conversationId);
            compressOldMessages(conversationId);
        }
    }

    @Transactional
    protected void compressOldMessages(String conversationId) {
        List<ChatRecord> uncompressedMessages = repository.findAllUncompressed(conversationId);
        
        if (uncompressedMessages.size() <= keepRecentMessages) {
            log.debug("Not enough messages to compress for conversation: {}", conversationId);
            return;
        }
        
        int messagesToCompress = uncompressedMessages.size() - keepRecentMessages;
        List<ChatRecord> oldMessages = uncompressedMessages.subList(0, messagesToCompress);
        
        String summary = generateSummary(oldMessages);
        
        ChatRecord summaryRecord = ChatRecord.compressedSummary(
            conversationId, 
            oldMessages.get(0).getUserId(), 
            summary
        );
        repository.save(summaryRecord);
        
        repository.deleteAll(oldMessages);
        
        log.info("Compressed {} messages into summary for conversation: {}", 
            messagesToCompress, conversationId);
    }

    private String generateSummary(List<ChatRecord> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatRecord msg : messages) {
            sb.append(String.format("[%s]: %s\n", msg.getRole(), msg.getContent()));
        }
        
        try {
            String summary = chatClient.prompt()
                .user("请总结以下对话的关键信息，保留重要的上下文和决策：\n\n" + sb.toString())
                .call()
                .content();
            
            return "【历史摘要】" + summary;
        } catch (Exception e) {
            log.warn("Failed to generate summary with LLM, using simple concatenation: {}", e.getMessage());
            return "【历史摘要】" + sb.substring(0, Math.min(sb.length(), 1000)) + "...";
        }
    }

    private int estimateTokenCount(String content) {
        if (content == null) return 0;
        return (int) (content.length() * 0.5);
    }

    @Transactional(readOnly = true)
    public List<ChatRecord> getCompressedSummaries(String conversationId) {
        log.debug("[DB] Getting compressed summaries for conversation: {}", conversationId);
        List<ChatRecord> summaries = repository.findCompressedSummaries(conversationId);
        log.debug("[DB] Found {} compressed summaries", summaries.size());
        return summaries;
    }

    @Transactional
    public void saveCompressedSummary(String conversationId, String userId, String content) {
        log.info("[DB] Saving compressed summary for conversation: {}", conversationId);
        
        ChatRecord summaryRecord = new ChatRecord();
        summaryRecord.setConversationId(conversationId);
        summaryRecord.setUserId(userId);
        summaryRecord.setRole("system");
        summaryRecord.setContent(content);
        summaryRecord.setTokenCount(estimateTokenCount(content));
        summaryRecord.setIsCompressed(true);
        
        repository.save(summaryRecord);
        log.info("[DB] Compressed summary saved, tokens: {}", summaryRecord.getTokenCount());
    }

    @Transactional
    public void markAllAsCompressed(String conversationId) {
        log.info("[DB] Marking all messages as compressed for conversation: {}", conversationId);
        List<ChatRecord> messages = repository.findAllUncompressed(conversationId);
        for (ChatRecord record : messages) {
            record.setIsCompressed(true);
        }
        repository.saveAll(messages);
        log.info("[DB] Marked {} messages as compressed", messages.size());
    }
}
