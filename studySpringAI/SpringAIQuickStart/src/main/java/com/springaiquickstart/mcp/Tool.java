package com.springaiquickstart.mcp;

import java.util.Map;

/**
 * 工具接口
 * 定义所有工具必须实现的方法
 */
public interface Tool {
    /**
     * 获取工具名称
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取工具参数定义
     * @return 参数定义（JSON Schema格式）
     */
    Map<String, Object> getParameters();
    
    /**
     * 执行工具
     * @param parameters 工具参数
     * @return 执行结果
     */
    Map<String, Object> execute(Map<String, Object> parameters);
}
