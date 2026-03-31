package com.springaiquickstart.controller;

import com.springaiquickstart.rag.DocumentLoaderService;
import com.springaiquickstart.rag.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RAG知识库管理控制器
 * 
 * 只提供知识库管理功能：
 * - 添加文档到知识库
 * - 删除文档
 * - 重载知识库
 * - 查看统计信息
 * 
 * 查询功能已集成到ChatController的chat方法中
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    /**
     * 添加文档到知识库
     */
    @PostMapping("/add")
    public Map<String, Object> addDocument(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "default") String category) {
        
        ragService.addDocument(title, content, category);
        
        return Map.of(
            "success", true,
            "message", "文档添加成功",
            "title", title,
            "category", category
        );
    }

    /**
     * 批量添加文档
     */
    @PostMapping("/add-batch")
    public Map<String, Object> addDocuments(@RequestBody List<Map<String, String>> documents) {
        List<DocumentLoaderService.TextChunk> chunks = documents.stream()
                .map(doc -> new DocumentLoaderService.TextChunk(
                    doc.get("title"),
                    doc.get("content"),
                    doc.getOrDefault("category", "default")
                ))
                .toList();
        
        ragService.addDocuments(chunks);
        
        return Map.of(
            "success", true,
            "message", "批量添加成功",
            "count", documents.size()
        );
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/document/{docId}")
    public Map<String, Object> deleteDocument(@PathVariable String docId) {
        ragService.deleteDocument(docId);
        return Map.of("success", true, "message", "文档删除成功", "doc_id", docId);
    }

    /**
     * 按分类删除文档
     */
    @DeleteMapping("/category/{category}")
    public Map<String, Object> deleteByCategory(@PathVariable String category) {
        ragService.deleteByCategory(category);
        return Map.of("success", true, "message", "分类删除成功", "category", category);
    }

    /**
     * 清空知识库
     */
    @DeleteMapping("/clear")
    public Map<String, Object> clearAll() {
        ragService.clearAll();
        return Map.of("success", true, "message", "知识库已清空");
    }

    /**
     * 重载默认知识库
     */
    @PostMapping("/reload")
    public Map<String, Object> reloadDefaultKnowledge() {
        ragService.reloadDefaultKnowledge();
        return Map.of(
            "success", true,
            "message", "默认知识库重载成功",
            "stats", ragService.getStats()
        );
    }
}
