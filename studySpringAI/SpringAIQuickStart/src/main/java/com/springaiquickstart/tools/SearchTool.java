package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索工具
 * 提供网络搜索功能（模拟）
 */
@Component
public class SearchTool implements Tool {

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "在网络上搜索信息";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> query = new HashMap<>();
        query.put("type", "string");
        query.put("description", "搜索关键词");
        properties.put("query", query);
        
        params.put("properties", properties);
        params.put("required", java.util.Arrays.asList("query"));
        
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String query = (String) parameters.get("query");
        
        Map<String, Object> result = new HashMap<>();
        
        // 模拟搜索结果
        result.put("query", query);
        result.put("results", java.util.Arrays.asList(
            Map.of("title", "搜索结果1", "url", "https://example.com/1", "snippet", "这是搜索结果1的摘要"),
            Map.of("title", "搜索结果2", "url", "https://example.com/2", "snippet", "这是搜索结果2的摘要"),
            Map.of("title", "搜索结果3", "url", "https://example.com/3", "snippet", "这是搜索结果3的摘要")
        ));
        result.put("status", "success");
        result.put("message", "搜索完成（模拟结果）");
        
        return result;
    }
}
