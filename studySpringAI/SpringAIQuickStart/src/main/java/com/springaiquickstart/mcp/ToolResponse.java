package com.springaiquickstart.mcp;

import java.util.Map;

/**
 * 工具响应
 * 定义工具执行结果的格式
 */
public class ToolResponse {
    private String callId;                // 对应调用的ID
    private Map<String, Object> result;   // 执行结果
    private boolean success;              // 是否成功
    private String error;                 // 错误信息（如果失败）
    
    // 构造方法
    public ToolResponse(String callId, Map<String, Object> result, boolean success, String error) {
        this.callId = callId;
        this.result = result;
        this.success = success;
        this.error = error;
    }
    
    // Getter方法
    public String getCallId() {
        return callId;
    }
    
    public Map<String, Object> getResult() {
        return result;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getError() {
        return error;
    }
}
