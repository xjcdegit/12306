package com.springai.rag.memory;

import com.springai.rag.memory.entity.ChatRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MemoryManager {

    private static final Logger log = LoggerFactory.getLogger(MemoryManager.class);

    private final InMemoryChatMemory shortTermMemory;
    private final RelationalDatabaseMemory longTermMemory;

    public MemoryManager(InMemoryChatMemory shortTermMemory, RelationalDatabaseMemory longTermMemory) {
        this.shortTermMemory = shortTermMemory;
        this.longTermMemory = longTermMemory;
    }

    public void addUserMessage(String conversationId, String userId, String content) {
        shortTermMemory.addUserMessage(conversationId, content);
        longTermMemory.addUserMessage(conversationId, userId, content);
        log.debug("Added user message to conversation: {}", conversationId);
    }

    public void addAssistantMessage(String conversationId, String userId, String content) {
        shortTermMemory.addAssistantMessage(conversationId, content);
        longTermMemory.addAssistantMessage(conversationId, userId, content);
        log.debug("Added assistant message to conversation: {}", conversationId);
    }

    public void addToolMessage(String conversationId, String userId, String content) {
        longTermMemory.addToolMessage(conversationId, userId, content);
        log.debug("Added tool message to conversation: {}", conversationId);
    }

    public List<Message> getContextForConversation(String conversationId, String userId, int recentCount) {
        List<Message> context = new ArrayList<>();
        
        List<ChatRecord> completeHistory = longTermMemory.getCompleteHistory(conversationId);
        for (ChatRecord record : completeHistory) {
            Message msg = recordToMessage(record);
            context.add(msg);
        }
        
        log.debug("Loaded {} messages from long-term memory for conversation: {}", 
            completeHistory.size(), conversationId);
        
        return context;
    }

    public List<Message> getRecentMessages(String conversationId, int lastN) {
        return shortTermMemory.get(conversationId, lastN);
    }

    public List<ChatRecord> getCompleteHistory(String conversationId) {
        return longTermMemory.getCompleteHistory(conversationId);
    }

    public List<ChatRecord> getUserHistory(String userId, int limit) {
        return longTermMemory.getUserHistory(userId, limit);
    }

    public void clearConversation(String conversationId) {
        shortTermMemory.clear(conversationId);
        longTermMemory.clearConversation(conversationId);
        log.info("Cleared all memory for conversation: {}", conversationId);
    }

    public MemoryStats getMemoryStats(String conversationId, String userId) {
        MemoryStats stats = new MemoryStats();
        stats.setShortTermMemoryCount(shortTermMemory.getMessageCount(conversationId));
        stats.setLongTermMemoryCount(longTermMemory.getMessageCount(conversationId));
        stats.setUserTotalMessageCount(longTermMemory.getUserMessageCount(userId));
        stats.setTotalTokenCount(longTermMemory.getTotalTokenCount(conversationId));
        return stats;
    }

    private Message recordToMessage(ChatRecord record) {
        return switch (record.getRole().toLowerCase()) {
            case "user" -> new UserMessage(record.getContent());
            case "assistant" -> new AssistantMessage(record.getContent());
            default -> new UserMessage(record.getContent());
        };
    }

    public static class MemoryStats {
        private int shortTermMemoryCount;
        private long longTermMemoryCount;
        private long userTotalMessageCount;
        private int totalTokenCount;

        public int getShortTermMemoryCount() { return shortTermMemoryCount; }
        public void setShortTermMemoryCount(int shortTermMemoryCount) { 
            this.shortTermMemoryCount = shortTermMemoryCount; 
        }
        
        public long getLongTermMemoryCount() { return longTermMemoryCount; }
        public void setLongTermMemoryCount(long longTermMemoryCount) { 
            this.longTermMemoryCount = longTermMemoryCount; 
        }
        
        public long getUserTotalMessageCount() { return userTotalMessageCount; }
        public void setUserTotalMessageCount(long userTotalMessageCount) { 
            this.userTotalMessageCount = userTotalMessageCount; 
        }
        
        public int getTotalTokenCount() { return totalTokenCount; }
        public void setTotalTokenCount(int totalTokenCount) { 
            this.totalTokenCount = totalTokenCount; 
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("shortTermMemoryCount", shortTermMemoryCount);
            map.put("longTermMemoryCount", longTermMemoryCount);
            map.put("userTotalMessageCount", userTotalMessageCount);
            map.put("totalTokenCount", totalTokenCount);
            return map;
        }
    }
}
