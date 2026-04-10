package com.springai.rag.memory.repository;

import com.springai.rag.memory.entity.ChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatRecord, Long> {

    List<ChatRecord> findByConversationIdOrderByTimestampAsc(String conversationId);

    List<ChatRecord> findByUserIdOrderByTimestampDesc(String userId);

    @Query("SELECT c FROM ChatRecord c WHERE c.conversationId = :conversationId AND c.isCompressed = false ORDER BY c.timestamp DESC LIMIT :limit")
    List<ChatRecord> findRecentUncompressed(@Param("conversationId") String conversationId, @Param("limit") int limit);

    @Query("SELECT c FROM ChatRecord c WHERE c.conversationId = :conversationId AND c.isCompressed = true ORDER BY c.timestamp DESC")
    List<ChatRecord> findCompressedSummaries(@Param("conversationId") String conversationId);

    @Query("SELECT c FROM ChatRecord c WHERE c.conversationId = :conversationId AND c.isCompressed = false ORDER BY c.timestamp ASC")
    List<ChatRecord> findAllUncompressed(@Param("conversationId") String conversationId);

    @Query("SELECT COUNT(c) FROM ChatRecord c WHERE c.conversationId = :conversationId")
    long countByConversationId(@Param("conversationId") String conversationId);

    @Query("SELECT COUNT(c) FROM ChatRecord c WHERE c.userId = :userId")
    long countByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(c.tokenCount), 0) FROM ChatRecord c WHERE c.conversationId = :conversationId AND c.isCompressed = false")
    int getTotalTokenCount(@Param("conversationId") String conversationId);

    void deleteByConversationId(String conversationId);

    @Query("SELECT c FROM ChatRecord c WHERE c.userId = :userId ORDER BY c.timestamp DESC LIMIT :limit")
    List<ChatRecord> findUserHistory(@Param("userId") String userId, @Param("limit") int limit);
}
