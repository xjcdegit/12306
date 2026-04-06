package com.springai.skills.core;

import java.util.Map;

/**
 * 工具执行结果 - Tool 执行后的返回值封装
 * 
 * <p>ToolResult 封装了 Tool 执行的结果，包含：
 * <ul>
 *   <li>执行状态（成功/失败）</li>
 *   <li>返回数据（成功时）</li>
 *   <li>错误信息（失败时）</li>
 * </ul></p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * // 成功场景
 * ToolResult result = ToolResult.success(queryResult);
 * 
 * // 失败场景
 * ToolResult result = ToolResult.failure("数据库连接失败");
 * </pre>
 * 
 * <h3>结果处理流程：</h3>
 * <pre>
 * Tool 执行 → 生成 ToolResult → Agent 接收 → 追加到对话上下文 → 发送给 LLM
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see Tool
 */
public class ToolResult {

    /**
     * 执行是否成功
     * true 表示工具执行成功，false 表示执行失败
     */
    private boolean success;

    /**
     * 返回数据
     * 成功时包含工具执行的结果数据，可以是任意 Java 对象
     * 通常为 Map、List 或 POJO
     */
    private Object data;

    /**
     * 错误信息
     * 失败时包含错误描述，用于告知 LLM 失败原因
     */
    private String error;

    /**
     * 默认构造函数
     */
    public ToolResult() {
    }

    /**
     * 全参数构造函数
     * 
     * @param success 执行是否成功
     * @param data 返回数据
     * @param error 错误信息
     */
    public ToolResult(boolean success, Object data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /**
     * 创建成功结果
     * 
     * <p>静态工厂方法，用于创建执行成功的 ToolResult。</p>
     * 
     * <p>使用场景：工具执行成功，需要返回数据给 LLM。</p>
     * 
     * @param data 返回的数据对象
     * @return 成功的 ToolResult 实例
     */
    public static ToolResult success(Object data) {
        return new ToolResult(true, data, null);
    }

    /**
     * 创建失败结果
     * 
     * <p>静态工厂方法，用于创建执行失败的 ToolResult。</p>
     * 
     * <p>使用场景：工具执行失败，需要告知 LLM 失败原因。</p>
     * 
     * @param error 错误描述信息
     * @return 失败的 ToolResult 实例
     */
    public static ToolResult failure(String error) {
        return new ToolResult(false, null, error);
    }

    /**
     * 判断执行是否成功
     * 
     * @return true 表示成功，false 表示失败
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置执行状态
     * 
     * @param success 执行状态
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取返回数据
     * 
     * @return 返回数据对象，失败时可能为 null
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置返回数据
     * 
     * @param data 返回数据
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 获取错误信息
     * 
     * @return 错误描述，成功时为 null
     */
    public String getError() {
        return error;
    }

    /**
     * 设置错误信息
     * 
     * @param error 错误描述
     */
    public void setError(String error) {
        this.error = error;
    }
}
