package com.springaiquickstart.mcp;

import java.util.Map;

/**
 * 工具调用请求
 * 定义工具调用的格式
 */
public class ToolCall {
    private String toolName;               // 工具名称
    private Map<String, Object> parameters; // 工具参数
    private String callId;                 // 调用ID（用于追踪）
    
    // 构造方法
    public ToolCall(String toolName, Map<String, Object> parameters, String callId) {
        this.toolName = toolName;
        this.parameters = parameters;
        this.callId = callId;
    }
    
    // Getter方法
    public String getToolName() {
        return toolName;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getCallId() {
        return callId;
    }
}
