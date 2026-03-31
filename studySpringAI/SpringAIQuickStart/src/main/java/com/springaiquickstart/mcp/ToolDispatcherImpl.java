package com.springaiquickstart.mcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工具调度器实现
 * 负责工具的调用和推荐
 */
@Component
public class ToolDispatcherImpl implements ToolDispatcher {
    private final ToolRegistry toolRegistry;
    
    /**
     * 构造方法
     * @param toolRegistry 工具注册表
     */
    @Autowired
    public ToolDispatcherImpl(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }
    
    @Override
    public ToolResponse dispatch(ToolCall toolCall) {
        // 根据工具名称获取工具实例
        Tool tool = toolRegistry.getToolByName(toolCall.getToolName());
        if (tool == null) {
            // 工具不存在
            return new ToolResponse(
                toolCall.getCallId(),
                null,
                false,
                "工具不存在: " + toolCall.getToolName()
            );
        }
        
        try {
            // 执行工具
            Map<String, Object> result = tool.execute(toolCall.getParameters());
            // 返回成功响应
            return new ToolResponse(
                toolCall.getCallId(),
                result,
                true,
                null
            );
        } catch (Exception e) {
            // 工具执行失败
            return new ToolResponse(
                toolCall.getCallId(),
                null,
                false,
                "工具执行失败: " + e.getMessage()
            );
        }
    }
    
    @Override
    public List<Tool> recommendTools(String query) {
        // 根据查询内容推荐合适的工具
        List<Tool> recommended = new ArrayList<>();
        for (Tool tool : toolRegistry.getAvailableTools()) {
            // 简单的关键词匹配
            if (query.contains("天气") && tool.getName().equals("weather_query")) {
                recommended.add(tool);
            } else if (query.contains("时间") && tool.getName().equals("time_query")) {
                recommended.add(tool);
            } else if (query.contains("计算") && tool.getName().equals("calculator")) {
                recommended.add(tool);
            } else if (query.contains("翻译") && tool.getName().equals("translation")) {
                recommended.add(tool);
            } else if (query.contains("搜索") && tool.getName().equals("search")) {
                recommended.add(tool);
            } else if (query.contains("邮件") && tool.getName().equals("email")) {
                recommended.add(tool);
            } else if (query.contains("SQL") && tool.getName().equals("sql_query")) {
                recommended.add(tool);
            }
        }
        return recommended;
    }
}
