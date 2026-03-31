package com.springaiquickstart.mcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表实现
 * 管理所有可用的工具
 */
@Component
public class ToolRegistryImpl implements ToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();
    
    /**
     * 构造方法
     * 自动注入所有实现了Tool接口的Bean
     * @param toolList 工具列表
     */
    @Autowired
    public ToolRegistryImpl(List<Tool> toolList) {
        // 自动注册所有实现了Tool接口的Bean
        for (Tool tool : toolList) {
            registerTool(tool);
        }
    }
    
    @Override
    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
    }
    
    @Override
    public List<Tool> getAvailableTools() {
        return new ArrayList<>(tools.values());
    }
    
    @Override
    public Tool getToolByName(String name) {
        return tools.get(name);
    }
    
    @Override
    public Map<String, Object> getToolDefinitions() {
        Map<String, Object> definitions = new HashMap<>();
        for (Tool tool : tools.values()) {
            definitions.put(tool.getName(), Map.of(
                "description", tool.getDescription(),
                "parameters", tool.getParameters()
            ));
        }
        return definitions;
    }
}
