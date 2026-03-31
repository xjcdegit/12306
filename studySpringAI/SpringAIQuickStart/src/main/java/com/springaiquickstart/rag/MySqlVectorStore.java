package com.springaiquickstart.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL向量存储
 * 实现文档的向量存储和相似度检索
 */
@Repository
public class MySqlVectorStore {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 文档实体类
     */
    public static class Document {
        private String docId;
        private String title;
        private String content;
        private String category;
        private List<Double> embedding;

        public Document(String docId, String title, String content, String category, List<Double> embedding) {
            this.docId = docId;
            this.title = title;
            this.content = content;
            this.category = category;
            this.embedding = embedding;
        }

        public String getDocId() { return docId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getCategory() { return category; }
        public List<Double> getEmbedding() { return embedding; }
    }

    /**
     * 插入文档向量
     */
    public void insertDocument(String docId, String title, String content, String category, List<Double> embedding) {
        String sql = "INSERT INTO knowledge_base (doc_id, title, content, category, embedding) VALUES (?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, docId);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, category);
            ps.setString(5, vectorToString(embedding));
            return ps;
        });
    }

    /**
     * 相似度检索
     */
    public List<Document> similaritySearch(List<Double> queryEmbedding, int topK) {
        String sql = "SELECT doc_id, title, content, category, embedding FROM knowledge_base";
        
        List<Document> allDocs = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String embeddingStr = rs.getString("embedding");
            List<Double> embedding = stringToVector(embeddingStr);
            return new Document(
                rs.getString("doc_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("category"),
                embedding
            );
        });

        // 计算相似度并排序
        List<Document> results = new ArrayList<>();
        allDocs.stream()
            .sorted((d1, d2) -> Double.compare(
                cosineSimilarity(queryEmbedding, d2.getEmbedding()),
                cosineSimilarity(queryEmbedding, d1.getEmbedding())
            ))
            .limit(topK)
            .forEach(results::add);

        return results;
    }

    /**
     * 按分类检索
     */
    public List<Document> similaritySearch(List<Double> queryEmbedding, int topK, String category) {
        String sql = "SELECT doc_id, title, content, category, embedding FROM knowledge_base WHERE category = ?";
        
        List<Document> allDocs = jdbcTemplate.query(sql, new Object[]{category}, (rs, rowNum) -> {
            String embeddingStr = rs.getString("embedding");
            List<Double> embedding = stringToVector(embeddingStr);
            return new Document(
                rs.getString("doc_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("category"),
                embedding
            );
        });

        List<Document> results = new ArrayList<>();
        allDocs.stream()
            .sorted((d1, d2) -> Double.compare(
                cosineSimilarity(queryEmbedding, d2.getEmbedding()),
                cosineSimilarity(queryEmbedding, d1.getEmbedding())
            ))
            .limit(topK)
            .forEach(results::add);

        return results;
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String docId) {
        String sql = "DELETE FROM knowledge_base WHERE doc_id = ?";
        jdbcTemplate.update(sql, docId);
    }

    /**
     * 按分类删除
     */
    public void deleteByCategory(String category) {
        String sql = "DELETE FROM knowledge_base WHERE category = ?";
        jdbcTemplate.update(sql, category);
    }

    /**
     * 清空所有文档
     */
    public void clearAll() {
        String sql = "DELETE FROM knowledge_base";
        jdbcTemplate.update(sql);
    }

    /**
     * 获取文档数量
     */
    public int getDocumentCount() {
        String sql = "SELECT COUNT(*) FROM knowledge_base";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * 获取所有分类
     */
    public List<String> getCategories() {
        String sql = "SELECT DISTINCT category FROM knowledge_base";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 向量转字符串
     */
    private String vectorToString(List<Double> vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(vector.get(i));
        }
        return sb.toString();
    }

    /**
     * 字符串转向量
     */
    private List<Double> stringToVector(String str) {
        List<Double> vector = new ArrayList<>();
        String[] parts = str.split(",");
        for (String part : parts) {
            vector.add(Double.parseDouble(part.trim()));
        }
        return vector;
    }
}
