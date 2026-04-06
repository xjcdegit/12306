package com.springai.skills.core;

import java.util.Map;

/**
 * 技能执行结果 - Skill 执行后的返回值封装
 * 
 * <p>SkillResult 封装了 Skill 执行的结果，包含：
 * <ul>
 *   <li>执行状态（成功/失败）</li>
 *   <li>返回数据（成功时）</li>
 *   <li>错误信息（失败时）</li>
 *   <li>所有 Tool 的执行结果（用于追踪和调试）</li>
 * </ul></p>
 * 
 * <h3>与 ToolResult 的区别：</h3>
 * <pre>
 * ToolResult：单个 Tool 的执行结果
 * SkillResult：整个工作流的执行结果，包含多个 ToolResult
 * </pre>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * // 成功场景 - 包含所有 Tool 的执行结果
 * Map&lt;String, ToolResult&gt; toolResults = new HashMap&lt;&gt;();
 * toolResults.put("query_database", queryResult);
 * toolResults.put("send_email", emailResult);
 * SkillResult result = SkillResult.success(finalData, toolResults);
 * 
 * // 失败场景
 * SkillResult result = SkillResult.failure("工作流执行失败：缺少必要参数");
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see Skill
 * @see ToolResult
 */
public class SkillResult {

    /**
     * 执行是否成功
     * true 表示技能执行成功，false 表示执行失败
     */
    private boolean success;

    /**
     * 返回数据
     * 成功时包含技能执行的最终结果数据
     * 通常是经过多个 Tool 处理后的聚合数据
     */
    private Object data;

    /**
     * 错误信息
     * 失败时包含错误描述，用于告知 LLM 失败原因
     */
    private String error;

    /**
     * 所有 Tool 的执行结果
     * 记录工作流中每个 Tool 的执行情况，用于：
     * <ul>
     *   <li>调试和追踪</li>
     *   <li>结果审计</li>
     *   <li>错误定位</li>
     * </ul>
     */
    private Map<String, ToolResult> toolResults;

    /**
     * 默认构造函数
     */
    public SkillResult() {
    }

    /**
     * 全参数构造函数
     * 
     * @param success 执行是否成功
     * @param data 返回数据
     * @param error 错误信息
     * @param toolResults 所有 Tool 的执行结果
     */
    public SkillResult(boolean success, Object data, String error, Map<String, ToolResult> toolResults) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.toolResults = toolResults;
    }

    /**
     * 创建成功结果
     * 
     * <p>静态工厂方法，用于创建执行成功的 SkillResult。</p>
     * 
     * <p>使用场景：技能执行成功，需要返回最终数据和所有 Tool 的执行记录。</p>
     * 
     * @param data 最终返回的数据对象
     * @param toolResults 所有 Tool 的执行结果映射
     * @return 成功的 SkillResult 实例
     */
    public static SkillResult success(Object data, Map<String, ToolResult> toolResults) {
        return new SkillResult(true, data, null, toolResults);
    }

    /**
     * 创建失败结果
     * 
     * <p>静态工厂方法，用于创建执行失败的 SkillResult。</p>
     * 
     * <p>使用场景：技能执行失败，需要告知 LLM 失败原因。</p>
     * 
     * @param error 错误描述信息
     * @return 失败的 SkillResult 实例
     */
    public static SkillResult failure(String error) {
        return new SkillResult(false, null, error, null);
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

    /**
     * 获取所有 Tool 的执行结果
     * 
     * <p>返回工作流中每个 Tool 的执行结果，Key 为 Tool 名称，Value 为执行结果。</p>
     * 
     * @return Tool 执行结果映射，失败时可能为 null
     */
    public Map<String, ToolResult> getToolResults() {
        return toolResults;
    }

    /**
     * 设置所有 Tool 的执行结果
     * 
     * @param toolResults Tool 执行结果映射
     */
    public void setToolResults(Map<String, ToolResult> toolResults) {
        this.toolResults = toolResults;
    }
}
