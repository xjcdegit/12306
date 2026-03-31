package com.springaiquickstart.rag;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文档加载服务
 * 负责将文本转换为向量并存储到向量数据库
 */
@Service
public class DocumentLoaderService {

    @Autowired
    private ZhipuAiClient zhipuAiClient;

    @Autowired
    private MySqlVectorStore vectorStore;

    /**
     * 文本块实体类
     */
    public static class TextChunk {
        private String title;
        private String content;
        private String category;

        public TextChunk(String title, String content, String category) {
            this.title = title;
            this.content = content;
            this.category = category;
        }

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getCategory() { return category; }
    }

    /**
     * 加载单个文档
     */
    public void loadDocument(String title, String content, String category) {
        List<Double> embedding = embedText(content);
        String docId = UUID.randomUUID().toString();
        vectorStore.insertDocument(docId, title, content, category, embedding);
    }

    /**
     * 批量加载文档
     */
    public void loadDocuments(List<TextChunk> documents) {
        for (TextChunk doc : documents) {
            loadDocument(doc.getTitle(), doc.getContent(), doc.getCategory());
        }
    }

    /**
     * 文本转向量
     * 使用智谱AI的embedding API
     */
    public List<Double> embedText(String text) {
        try {
            // 使用智谱AI的embedding API
            // 这里简化实现，返回一个模拟的向量
            // 实际使用时需要调用智谱AI的embedding接口
            List<Double> embedding = new ArrayList<>();
            for (int i = 0; i < 1024; i++) {
                embedding.add(Math.random());
            }
            return embedding;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 返回空向量
        return new ArrayList<>();
    }

    /**
     * 加载默认知识库
     */
    public void loadDefaultKnowledge() {
        List<TextChunk> documents = new ArrayList<>();
        
        documents.add(new TextChunk(
            "公司简介",
            "我们是一家专注于人工智能技术研发的高科技企业，成立于2020年。公司主要业务包括自然语言处理、计算机视觉、智能推荐等领域。",
            "company"
        ));
        
        documents.add(new TextChunk(
            "产品介绍",
            "我们的主要产品是AI助手平台，提供智能客服、智能写作、智能分析等功能。平台支持多种大模型接入，包括GPT、GLM等。",
            "product"
        ));
        
        documents.add(new TextChunk(
            "技术架构",
            "系统采用微服务架构，使用Spring Cloud作为微服务框架，MySQL作为关系型数据库，Redis作为缓存，向量数据库用于知识检索。",
            "technology"
        ));
        
        documents.add(new TextChunk(
            "联系方式",
            "公司地址：北京市海淀区中关村软件园。联系电话：010-12345678。邮箱：contact@example.com。",
            "contact"
        ));
        
        loadDocuments(documents);
    }
}
