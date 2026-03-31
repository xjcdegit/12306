package com.springaiquickstart.mcp;

import java.util.List;
import java.util.Map;

/**
 * 工具注册表
 * 管理所有可用的工具
 */
public interface ToolRegistry {
    /**
     * 注册工具
     * @param tool 工具实例
     */
    void registerTool(Tool tool);
    
    /**
     * 获取所有可用工具
     * @return 工具列表
     */
    List<Tool> getAvailableTools();
    
    /**
     * 根据名称获取工具
     * @param name 工具名称
     * @return 工具实例
     */
    Tool getToolByName(String name);
    
    /**
     * 获取所有工具的定义
     * @return 工具定义映射
     */
    Map<String, Object> getToolDefinitions();
}
