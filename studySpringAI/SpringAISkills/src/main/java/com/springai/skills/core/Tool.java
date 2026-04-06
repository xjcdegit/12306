package com.springai.skills.core;

import java.util.Map;

/**
 * 原子工具接口 - 最小操作单元
 * 
 * <p>Tool 是系统中可被 LLM 直接调用的最小操作单元。
 * 每个 Tool 封装一个具体的原子操作，如查询数据库、调用 API、发送邮件等。</p>
 * 
 * <h3>工作流程：</h3>
 * <pre>
 * 1. LLM 分析用户意图，决定调用哪个 Tool
 * 2. LLM 生成结构化调用指令（包含 Tool 名称和参数）
 * 3. Agent 通过 MCP 协议调用 Tool
 * 4. Tool 执行具体操作并返回结果
 * </pre>
 * 
 * <h3>设计原则：</h3>
 * <ul>
 *   <li>单一职责：每个 Tool 只做一件事</li>
 *   <li>输入输出结构化：使用 JSON Schema 描述参数</li>
 *   <li>无状态：Tool 不应保存调用间的状态</li>
 * </ul>
 * 
 * @author SpringAI Skills Framework
 * @see ToolResult
 * @see com.springai.skills.registry.ToolRegistry
 */
public interface Tool {

    /**
     * 获取工具名称
     * 
     * <p>名称是 Tool 的唯一标识符，用于 LLM 选择和调用。
     * 建议使用 snake_case 命名风格，如：query_database、send_email</p>
     * 
     * @return 工具名称，不能为空
     */
    String getName();

    /**
     * 获取工具描述
     * 
     * <p>描述用于告知 LLM 该工具的功能和用途。
     * LLM 会根据描述来判断是否需要调用此工具。</p>
     * 
     * <p>示例："查询数据库并返回结果"</p>
     * 
     * @return 工具描述，建议 50-200 字符
     */
    String getDescription();

    /**
     * 获取参数 Schema
     * 
     * <p>返回 JSON Schema 格式的参数描述，用于：
     * <ul>
     *   <li>告知 LLM 该工具需要哪些参数</li>
     *   <li>参数的类型和约束</li>
     *   <li>哪些参数是必需的</li>
     * </ul></p>
     * 
     * <p>示例返回值：</p>
     * <pre>
     * {
     *   "type": "object",
     *   "properties": {
     *     "query": {
     *       "type": "string",
     *       "description": "SQL 查询语句"
     *     }
     *   },
     *   "required": ["query"]
     * }
     * </pre>
     * 
     * @return JSON Schema 格式的参数描述
     */
    Map<String, Object> getParametersSchema();

    /**
     * 执行工具操作
     * 
     * <p>这是 Tool 的核心方法，执行具体的业务逻辑。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 验证参数有效性
     * 2. 执行具体操作（如数据库查询、API 调用）
     * 3. 封装结果为 ToolResult
     * 4. 返回结果
     * </pre>
     * 
     * <h3>异常处理：</h3>
     * <p>方法不应抛出异常，所有错误应通过 ToolResult.failure() 返回。</p>
     * 
     * @param parameters 工具参数，由 LLM 生成或用户传入
     * @return 执行结果，包含成功/失败状态、数据或错误信息
     */
    ToolResult execute(Map<String, Object> parameters);
}
