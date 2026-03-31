package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL查询工具
 * 提供数据库查询功能（模拟）
 */
@Component
public class SqlTool implements Tool {

    @Override
    public String getName() {
        return "sql_query";
    }

    @Override
    public String getDescription() {
        return "执行SQL查询语句";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> sql = new HashMap<>();
        sql.put("type", "string");
        sql.put("description", "SQL查询语句");
        properties.put("sql", sql);
        
        params.put("properties", properties);
        params.put("required", java.util.Arrays.asList("sql"));
        
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String sql = (String) parameters.get("sql");
        
        Map<String, Object> result = new HashMap<>();
        
        // 模拟SQL查询
        result.put("sql", sql);
        result.put("rows", java.util.Arrays.asList(
            Map.of("id", 1, "name", "张三", "age", 25),
            Map.of("id", 2, "name", "李四", "age", 30),
            Map.of("id", 3, "name", "王五", "age", 28)
        ));
        result.put("rowCount", 3);
        result.put("status", "success");
        result.put("message", "查询执行成功（模拟结果）");
        
        return result;
    }
}
