package com.springai.rag.memory.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_history", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_is_compressed", columnList = "is_compressed"),
    @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp")
})
public class ChatRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 100)
    private String conversationId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "is_compressed")
    private Boolean isCompressed = false;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
    }

    public static ChatRecord userMessage(String conversationId, String userId, String content) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("user");
        record.setContent(content);
        record.setIsCompressed(false);
        record.setTimestamp(LocalDateTime.now());
        return record;
    }

    public static ChatRecord assistantMessage(String conversationId, String userId, String content) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("assistant");
        record.setContent(content);
        record.setIsCompressed(false);
        record.setTimestamp(LocalDateTime.now());
        return record;
    }

    public static ChatRecord toolMessage(String conversationId, String userId, String content) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("tool");
        record.setContent(content);
        record.setIsCompressed(false);
        record.setTimestamp(LocalDateTime.now());
        return record;
    }

    public static ChatRecord compressedSummary(String conversationId, String userId, String summary) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setRole("system");
        record.setContent("【历史摘要】");
        record.setSummary(summary);
        record.setIsCompressed(true);
        record.setTimestamp(LocalDateTime.now());
        return record;
    }
}
