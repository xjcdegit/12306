package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import com.springaiquickstart.mcp.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 工具管理器
 * 
 * 功能说明：
 * 集中管理和注册所有工具，提供统一的工具调用接口。
 * 利用MCP的ToolRegistry来管理工具，避免重复注册。
 * 
 * 主要职责：
 * 1. 利用ToolRegistry管理工具
 * 2. 提供工具定义列表（适配Spring AI格式）
 * 3. 执行工具调用
 * 4. 处理工具调用错误
 */
@Component
public class ToolManager {

    private static final Logger logger = LoggerFactory.getLogger(ToolManager.class);

    /**
     * 工具注册表
     */
    private final ToolRegistry toolRegistry;

    @Autowired
    public ToolManager(ToolRegistry toolRegistry) {
        
        logger.info("========== ToolManager 初始化开始 ==========");
        
        this.toolRegistry = toolRegistry;
        
        logger.info("========== ToolManager 初始化完成 ==========");
        logger.info("已注册的工具数量: {}", toolRegistry.getAvailableTools().size());
        
        List<Tool> tools = toolRegistry.getAvailableTools();
        for (Tool tool : tools) {
            logger.info("  - {}", tool.getName());
        }
    }

    /**
     * 获取所有工具定义（适配Spring AI格式）
     */
    public List<Map<String, Object>> getToolDefinitions() {
        logger.info("========== getToolDefinitions() 被调用 ==========");
        
        List<Map<String, Object>> definitions = new ArrayList<>();
        List<Tool> tools = toolRegistry.getAvailableTools();
        
        for (Tool tool : tools) {
            definitions.add(createToolDefinition(tool));
        }
        
        logger.info("返回 {} 个工具定义", definitions.size());
        
        return definitions;
    }
    
    /**
     * 创建工具定义（Spring AI格式）
     */
    private Map<String, Object> createToolDefinition(Tool tool) {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", tool.getName(),
                        "description", tool.getDescription(),
                        "parameters", tool.getParameters()
                )
        );
    }

    /**
     * 执行工具调用
     */
    public Map<String, Object> executeTool(String toolName, Map<String, Object> parameters) {
        logger.info("========== executeTool() 被调用 ==========");
        logger.info("工具名称: {}", toolName);
        logger.info("工具参数: {}", parameters);
        
        Tool tool = toolRegistry.getToolByName(toolName);
        
        if (tool == null) {
            logger.error("工具不存在: {}", toolName);
            logger.error("可用的工具: {}", toolRegistry.getAvailableTools().stream().map(Tool::getName).toList());
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        
        try {
            logger.info("开始执行工具: {}", toolName);
            Map<String, Object> result = tool.execute(parameters);
            logger.info("工具执行成功，结果: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("工具执行失败: {}", e.getMessage(), e);
            return Map.of(
                    "tool_call_id", toolName + "-error-" + System.currentTimeMillis(),
                    "function_name", toolName,
                    "output", Map.of(
                            "error", "工具执行失败: " + e.getMessage(),
                            "tool_name", toolName
                    )
            );
        }
    }

    /**
     * 获取所有已注册的工具名称
     */
    public List<String> getRegisteredToolNames() {
        List<Tool> tools = toolRegistry.getAvailableTools();
        List<String> names = new ArrayList<>();
        for (Tool tool : tools) {
            names.add(tool.getName());
        }
        return names;
    }

    /**
     * 检查工具是否已注册
     */
    public boolean isToolRegistered(String toolName) {
        return toolRegistry.getToolByName(toolName) != null;
    }
}
