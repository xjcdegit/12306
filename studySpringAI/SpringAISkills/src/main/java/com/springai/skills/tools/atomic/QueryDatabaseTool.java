package com.springai.skills.tools.atomic;

import com.springai.skills.core.Tool;
import com.springai.skills.core.ToolResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库查询工具 - 原子 Tool 示例实现
 * 
 * <p>QueryDatabaseTool 是一个原子级别的工具，负责执行 SQL 查询并返回结果。
 * 它是 Skill 工作流中最基础的构建块之一。</p>
 * 
 * <h3>IoC 设计说明：</h3>
 * <p>该类使用 @Component 注解，由 Spring 容器自动创建和管理。
 * 注册逻辑由 DemoToolsConfiguration 统一处理。</p>
 * 
 * @author SpringAI Skills Framework
 * @see Tool
 * @see ToolResult
 */
@Component
public class QueryDatabaseTool implements Tool {

    @Override
    public String getName() {
        return "query_database";
    }

    @Override
    public String getDescription() {
        return "Execute a SQL query on the database and return results";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> queryProp = new HashMap<>();
        queryProp.put("type", "string");
        queryProp.put("description", "The SQL query to execute");
        properties.put("query", queryProp);
        
        schema.put("properties", properties);
        schema.put("required", java.util.List.of("query"));
        return schema;
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        String query = (String) parameters.get("query");
        if (query == null || query.isEmpty()) {
            return ToolResult.failure("Query parameter is required");
        }

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("query", query);
        mockResult.put("rows", 10);
        mockResult.put("data", java.util.List.of(
            Map.of("id", 1, "name", "User1", "score", 9500),
            Map.of("id", 2, "name", "User2", "score", 8800)
        ));

        return ToolResult.success(mockResult);
    }
}
