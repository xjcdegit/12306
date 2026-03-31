package com.springaiquickstart.mcp;

import java.util.List;

/**
 * 工具调度器
 * 负责工具的调用和推荐
 */
public interface ToolDispatcher {
    /**
     * 调度工具执行
     * @param toolCall 工具调用请求
     * @return 工具响应
     */
    ToolResponse dispatch(ToolCall toolCall);
    
    /**
     * 根据查询推荐工具
     * @param query 用户查询
     * @return 推荐的工具列表
     */
    List<Tool> recommendTools(String query);
}
