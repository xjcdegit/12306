package com.springai.rag.memory;

import com.springai.rag.memory.entity.ChatRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private static final int MAX_SHORT_TERM_MESSAGES = 50;
    private static final int WARNING_THRESHOLD = 30;

    private final Map<String, InMemoryChatMemory> sessions = new ConcurrentHashMap<>();
    private final Map<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();
    private final RelationalDatabaseMemory longTermMemory;
    private final MemoryCompressor memoryCompressor;

    public SessionManager(RelationalDatabaseMemory longTermMemory, MemoryCompressor memoryCompressor) {
        this.longTermMemory = longTermMemory;
        this.memoryCompressor = memoryCompressor;
        log.info("[SESSION] SessionManager initialized");
    }

    public SessionStatus initSession(String conversationId, String userId) {
        log.info("========================================");
        log.info("[SESSION] Initializing session: {}", conversationId);
        log.info("[SESSION] User ID: {}", userId);

        if (sessions.containsKey(conversationId)) {
            log.info("[SESSION] Session already exists, returning existing session");
            return getSessionStatus(conversationId);
        }

        InMemoryChatMemory memory = new InMemoryChatMemory();
        
        List<ChatRecord> history = longTermMemory.getCompressedSummaries(conversationId);
        log.info("[SESSION] Loaded {} compressed summaries from long-term memory", history.size());

        for (ChatRecord record : history) {
            Message msg = recordToMessage(record);
            memory.add(conversationId, Collections.singletonList(msg));
        }

        sessions.put(conversationId, memory);
        
        SessionMetadata metadata = new SessionMetadata();
        metadata.setConversationId(conversationId);
        metadata.setUserId(userId);
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setLastSaved(LocalDateTime.now());
        metadata.setDirty(false);
        sessionMetadata.put(conversationId, metadata);

        SessionStatus status = getSessionStatus(conversationId);
        log.info("[SESSION] Session initialized with {} messages", status.getShortTermCount());
        log.info("========================================");
        
        return status;
    }

    public InMemoryChatMemory getMemory(String conversationId) {
        if (!sessions.containsKey(conversationId)) {
            log.warn("[SESSION] Session not found: {}, creating new session", conversationId);
            initSession(conversationId, "unknown");
        }
        return sessions.get(conversationId);
    }

    public void addUserMessage(String conversationId, String content) {
        InMemoryChatMemory memory = getMemory(conversationId);
        memory.addUserMessage(conversationId, content);
        markDirty(conversationId);
        log.debug("[SESSION] Added user message to session: {}", conversationId);
    }

    public void addAssistantMessage(String conversationId, String content) {
        InMemoryChatMemory memory = getMemory(conversationId);
        memory.addAssistantMessage(conversationId, content);
        markDirty(conversationId);
        log.debug("[SESSION] Added assistant message to session: {}", conversationId);
    }

    public List<Message> getContext(String conversationId, int lastN) {
        InMemoryChatMemory memory = getMemory(conversationId);
        return memory.get(conversationId, lastN);
    }

    public CompressionResult saveAndCompress(String conversationId) {
        log.info("========================================");
        log.info("[SESSION] Saving and compressing session: {}", conversationId);

        InMemoryChatMemory memory = getMemory(conversationId);
        SessionMetadata metadata = sessionMetadata.get(conversationId);

        List<Message> allMessages = memory.get(conversationId, Integer.MAX_VALUE);
        int originalCount = allMessages.size();
        int originalTokens = estimateTokens(allMessages);

        log.info("[SESSION] Original messages: {}, estimated tokens: {}", originalCount, originalTokens);

        String compressedContent = memoryCompressor.compress(allMessages);
        int compressedTokens = estimateTokens(compressedContent);

        log.info("[SESSION] Compressed to {} tokens", compressedTokens);

        longTermMemory.saveCompressedSummary(conversationId, metadata.getUserId(), compressedContent);

        memory.clear(conversationId);

        Message compressedMessage = new AssistantMessage("[会话摘要]\n" + compressedContent);
        memory.add(conversationId, Collections.singletonList(compressedMessage));

        metadata.setDirty(false);
        metadata.setLastSaved(LocalDateTime.now());

        CompressionResult result = new CompressionResult();
        result.setSuccess(true);
        result.setOriginalMessages(originalCount);
        result.setCompressedTo(1);
        result.setOriginalTokens(originalTokens);
        result.setCompressedTokens(compressedTokens);
        result.setCompressionRatio(String.format("%.1f%%", (1 - (double) compressedTokens / originalTokens) * 100));

        log.info("[SESSION] Save complete. Compression ratio: {}", result.getCompressionRatio());
        log.info("========================================");

        return result;
    }

    public SessionStatus getSessionStatus(String conversationId) {
        InMemoryChatMemory memory = sessions.get(conversationId);
        SessionMetadata metadata = sessionMetadata.get(conversationId);

        SessionStatus status = new SessionStatus();
        status.setConversationId(conversationId);

        if (memory != null) {
            int count = memory.getMessageCount(conversationId);
            status.setShortTermCount(count);
            status.setMemoryUsage(String.format("%.0f%%", (double) count / MAX_SHORT_TERM_MESSAGES * 100));
            
            if (count >= MAX_SHORT_TERM_MESSAGES) {
                status.setWarning("记忆已达上限，请保存后继续");
                status.setCanChat(false);
            } else if (count >= WARNING_THRESHOLD) {
                status.setWarning("记忆较多，建议保存以获得更好体验");
                status.setCanChat(true);
            } else {
                status.setWarning(null);
                status.setCanChat(true);
            }
        }

        if (metadata != null) {
            status.setDirty(metadata.isDirty());
            status.setLastSaved(metadata.getLastSaved());
            status.setUserId(metadata.getUserId());
        }

        return status;
    }

    public void discardChanges(String conversationId) {
        log.info("[SESSION] Discarding changes for session: {}", conversationId);
        
        InMemoryChatMemory memory = sessions.get(conversationId);
        if (memory != null) {
            memory.clear(conversationId);
        }

        SessionMetadata metadata = sessionMetadata.get(conversationId);
        if (metadata != null) {
            metadata.setDirty(false);
        }

        List<ChatRecord> history = longTermMemory.getCompressedSummaries(conversationId);
        if (memory != null) {
            for (ChatRecord record : history) {
                Message msg = recordToMessage(record);
                memory.add(conversationId, Collections.singletonList(msg));
            }
        }
    }

    public void markDirty(String conversationId) {
        SessionMetadata metadata = sessionMetadata.get(conversationId);
        if (metadata != null) {
            metadata.setDirty(true);
        }
    }

    public boolean isSessionDirty(String conversationId) {
        SessionMetadata metadata = sessionMetadata.get(conversationId);
        return metadata != null && metadata.isDirty();
    }

    public void closeSession(String conversationId) {
        log.info("[SESSION] Closing session: {}", conversationId);
        sessions.remove(conversationId);
        sessionMetadata.remove(conversationId);
    }

    public List<String> getActiveSessions() {
        return new ArrayList<>(sessions.keySet());
    }

    private Message recordToMessage(ChatRecord record) {
        return switch (record.getRole().toLowerCase()) {
            case "user" -> new UserMessage(record.getContent());
            case "assistant" -> new AssistantMessage(record.getContent());
            default -> new UserMessage(record.getContent());
        };
    }

    private int estimateTokens(List<Message> messages) {
        return messages.stream()
            .mapToInt(m -> m.getText().length() / 4)
            .sum();
    }

    private int estimateTokens(String content) {
        return content.length() / 4;
    }

    public static class SessionMetadata {
        private String conversationId;
        private String userId;
        private LocalDateTime createdAt;
        private LocalDateTime lastSaved;
        private boolean dirty;

        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getLastSaved() { return lastSaved; }
        public void setLastSaved(LocalDateTime lastSaved) { this.lastSaved = lastSaved; }
        
        public boolean isDirty() { return dirty; }
        public void setDirty(boolean dirty) { this.dirty = dirty; }
    }

    public static class SessionStatus {
        private String conversationId;
        private String userId;
        private int shortTermCount;
        private boolean dirty;
        private LocalDateTime lastSaved;
        private String memoryUsage;
        private String warning;
        private boolean canChat;

        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public int getShortTermCount() { return shortTermCount; }
        public void setShortTermCount(int shortTermCount) { this.shortTermCount = shortTermCount; }
        
        public boolean isDirty() { return dirty; }
        public void setDirty(boolean dirty) { this.dirty = dirty; }
        
        public LocalDateTime getLastSaved() { return lastSaved; }
        public void setLastSaved(LocalDateTime lastSaved) { this.lastSaved = lastSaved; }
        
        public String getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(String memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public String getWarning() { return warning; }
        public void setWarning(String warning) { this.warning = warning; }
        
        public boolean isCanChat() { return canChat; }
        public void setCanChat(boolean canChat) { this.canChat = canChat; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("conversationId", conversationId);
            map.put("userId", userId);
            map.put("shortTermCount", shortTermCount);
            map.put("dirty", dirty);
            map.put("lastSaved", lastSaved != null ? lastSaved.toString() : null);
            map.put("memoryUsage", memoryUsage);
            map.put("warning", warning);
            map.put("canChat", canChat);
            return map;
        }
    }

    public static class CompressionResult {
        private boolean success;
        private int originalMessages;
        private int compressedTo;
        private int originalTokens;
        private int compressedTokens;
        private String compressionRatio;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public int getOriginalMessages() { return originalMessages; }
        public void setOriginalMessages(int originalMessages) { this.originalMessages = originalMessages; }
        
        public int getCompressedTo() { return compressedTo; }
        public void setCompressedTo(int compressedTo) { this.compressedTo = compressedTo; }
        
        public int getOriginalTokens() { return originalTokens; }
        public void setOriginalTokens(int originalTokens) { this.originalTokens = originalTokens; }
        
        public int getCompressedTokens() { return compressedTokens; }
        public void setCompressedTokens(int compressedTokens) { this.compressedTokens = compressedTokens; }
        
        public String getCompressionRatio() { return compressionRatio; }
        public void setCompressionRatio(String compressionRatio) { this.compressionRatio = compressionRatio; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("success", success);
            map.put("originalMessages", originalMessages);
            map.put("compressedTo", compressedTo);
            map.put("originalTokens", originalTokens);
            map.put("compressedTokens", compressedTokens);
            map.put("compressionRatio", compressionRatio);
            return map;
        }
    }
}
