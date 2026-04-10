package com.springai.rag.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryChatMemory {

    private static final Logger log = LoggerFactory.getLogger(InMemoryChatMemory.class);

    private final Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

    private final int maxMessagesPerConversation;

    public InMemoryChatMemory() {
        this.maxMessagesPerConversation = 20;
    }

    public InMemoryChatMemory(int maxMessagesPerConversation) {
        this.maxMessagesPerConversation = maxMessagesPerConversation;
    }

    public void add(String conversationId, List<Message> messages) {
        List<Message> history = conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>());
        
        for (Message message : messages) {
            history.add(message);
            log.debug("Added message to conversation {}: {}", conversationId, message.getMessageType());
        }
        
        while (history.size() > maxMessagesPerConversation) {
            history.remove(0);
            log.debug("Removed oldest message from conversation {} due to size limit", conversationId);
        }
    }

    public List<Message> get(String conversationId, int lastN) {
        List<Message> history = conversationHistory.get(conversationId);
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        
        int start = Math.max(0, history.size() - lastN);
        return new ArrayList<>(history.subList(start, history.size()));
    }

    public void clear(String conversationId) {
        conversationHistory.remove(conversationId);
        log.info("Cleared memory for conversation: {}", conversationId);
    }

    public int getMessageCount(String conversationId) {
        List<Message> history = conversationHistory.get(conversationId);
        return history != null ? history.size() : 0;
    }

    public List<Message> getAllMessages(String conversationId) {
        List<Message> history = conversationHistory.get(conversationId);
        return history != null ? new ArrayList<>(history) : new ArrayList<>();
    }

    public void addUserMessage(String conversationId, String content) {
        add(conversationId, List.of(new UserMessage(content)));
    }

    public void addAssistantMessage(String conversationId, String content) {
        add(conversationId, List.of(new AssistantMessage(content)));
    }

    public void addSystemMessage(String conversationId, String content) {
        add(conversationId, List.of(new SystemMessage(content)));
    }

    public boolean hasConversation(String conversationId) {
        return conversationHistory.containsKey(conversationId);
    }

    public int getTotalConversationCount() {
        return conversationHistory.size();
    }

    public void clearAll() {
        conversationHistory.clear();
        log.info("Cleared all conversations from memory");
    }
}
