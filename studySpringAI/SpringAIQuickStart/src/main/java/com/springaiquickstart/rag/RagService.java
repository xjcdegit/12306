package com.springaiquickstart.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG检索增强生成服务
 * 
 * 功能说明：
 * 1. 接收用户问题
 * 2. 将问题向量化
 * 3. 从向量数据库检索相关文档
 * 4. 构建增强Prompt（上下文 + 问题）
 * 5. 返回检索结果供LLM使用
 */
@Service
public class RagService {

    private final MySqlVectorStore vectorStore;
    private final DocumentLoaderService documentLoader;

    public RagService(MySqlVectorStore vectorStore, DocumentLoaderService documentLoader) {
        this.vectorStore = vectorStore;
        this.documentLoader = documentLoader;
    }

    public Map<String, Object> query(String question, int topK) {
        List<Double> queryEmbedding = documentLoader.embedText(question);
        List<MySqlVectorStore.Document> docs = vectorStore.similaritySearch(queryEmbedding, topK);
        
        return buildResponse(question, docs);
    }

    public Map<String, Object> query(String question, int topK, String category) {
        List<Double> queryEmbedding = documentLoader.embedText(question);
        List<MySqlVectorStore.Document> docs = vectorStore.similaritySearch(queryEmbedding, topK, category);
        
        return buildResponse(question, docs);
    }

    private Map<String, Object> buildResponse(String question, List<MySqlVectorStore.Document> docs) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("question", question);
        result.put("document_count", docs.size());
        
        List<Map<String, Object>> documents = new ArrayList<>();
        StringBuilder contextBuilder = new StringBuilder();
        
        for (int i = 0; i < docs.size(); i++) {
            MySqlVectorStore.Document doc = docs.get(i);
            
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("doc_id", doc.getDocId());
            docMap.put("title", doc.getTitle());
            docMap.put("content", doc.getContent());
            docMap.put("category", doc.getCategory());
            documents.add(docMap);
            
            contextBuilder.append("【文档").append(i + 1).append("】\n");
            contextBuilder.append("标题：").append(doc.getTitle()).append("\n");
            contextBuilder.append("内容：").append(doc.getContent()).append("\n\n");
        }
        
        result.put("documents", documents);
        result.put("context", contextBuilder.toString());
        
        String enhancedPrompt = buildEnhancedPrompt(question, contextBuilder.toString());
        result.put("enhanced_prompt", enhancedPrompt);
        
        return result;
    }

    private String buildEnhancedPrompt(String question, String context) {
        return """
            你是一个专业的AI助手。请根据以下上下文信息回答用户的问题。
            如果上下文中没有相关信息，请明确告知用户，不要编造答案。
            
            ===上下文信息===
            %s
            
            ===用户问题===
            %s
            
            ===回答要求===
            1. 基于上下文信息准确回答
            2. 如果上下文信息不足，请诚实说明
            3. 使用简洁清晰的语言
            4. 必要时可以引用具体的文档内容
            """.formatted(context, question);
    }

    public void addDocument(String title, String content, String category) {
        documentLoader.loadDocument(title, content, category);
    }

    public void addDocuments(List<DocumentLoaderService.TextChunk> documents) {
        documentLoader.loadDocuments(documents);
    }

    public void deleteDocument(String docId) {
        vectorStore.deleteDocument(docId);
    }

    public void deleteByCategory(String category) {
        vectorStore.deleteByCategory(category);
    }

    public void clearAll() {
        vectorStore.clearAll();
    }

    public void reloadDefaultKnowledge() {
        vectorStore.clearAll();
        documentLoader.loadDefaultKnowledge();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_documents", vectorStore.getDocumentCount());
        stats.put("categories", vectorStore.getCategories());
        return stats;
    }
}
