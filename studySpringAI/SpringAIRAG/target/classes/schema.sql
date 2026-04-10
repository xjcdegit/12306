-- SpringAIRAG 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS spring_ai_rag 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE spring_ai_rag;

-- 创建聊天历史表
CREATE TABLE IF NOT EXISTS chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    user_id VARCHAR(100) NOT NULL COMMENT '用户ID',
    role VARCHAR(50) NOT NULL COMMENT '消息角色: user/assistant/system/tool',
    content TEXT COMMENT '消息内容',
    token_count INT DEFAULT 0 COMMENT 'Token数量',
    is_compressed BOOLEAN DEFAULT FALSE COMMENT '是否为压缩摘要',
    summary TEXT COMMENT '压缩摘要内容',
    timestamp DATETIME NOT NULL COMMENT '消息时间戳',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_compressed (is_compressed),
    INDEX idx_user_timestamp (user_id, timestamp),
    INDEX idx_conversation_timestamp (conversation_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天历史记录表';

-- 创建向量存储表（可选，用于RAG）
CREATE TABLE IF NOT EXISTS vector_store (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT NOT NULL COMMENT '文档内容',
    metadata JSON COMMENT '元数据',
    embedding BLOB COMMENT '向量嵌入',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='向量存储表';

-- 创建文档表（用于RAG）
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) COMMENT '文件名',
    content_type VARCHAR(100) COMMENT '内容类型',
    content LONGTEXT COMMENT '文档内容',
    chunk_index INT DEFAULT 0 COMMENT '分块索引',
    total_chunks INT DEFAULT 1 COMMENT '总分块数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_filename (filename),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档存储表';
