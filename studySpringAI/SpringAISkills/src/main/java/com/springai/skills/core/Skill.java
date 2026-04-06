package com.springai.skills.core;

import java.util.List;
import java.util.Map;

/**
 * 技能接口 - 工作流封装
 * 
 * <p>Skill 是业务层概念，代表一个完整的业务工作流。
 * 它封装了多个原子 Tool 的组合调用，完成复杂的业务目标。</p>
 * 
 * <h3>Skill 与 Tool 的关系：</h3>
 * <pre>
 * Skill（工作流）
 * ├── 原子 Tool A（如：查询数据库）
 * ├── 原子 Tool B（如：处理数据）
 * └── 原子 Tool C（如：发送邮件）
 * </pre>
 * 
 * <h3>工作流程：</h3>
 * <pre>
 * 1. LLM 根据用户意图选择合适的 Skill
 * 2. Agent 调用 Skill 的 execute 方法
 * 3. Skill 内部按预定顺序调用多个 Tool
 * 4. Skill 汇总所有 Tool 的执行结果
 * 5. 返回 SkillResult 给 Agent
 * </pre>
 * 
 * <h3>设计原则：</h3>
 * <ul>
 *   <li>业务导向：Skill 应对应一个完整的业务场景</li>
 *   <li>流程编排：Skill 负责协调多个 Tool 的执行顺序</li>
 *   <li>结果聚合：Skill 汇总所有 Tool 的执行结果</li>
 * </ul>
 * 
 * @author SpringAI Skills Framework
 * @see SkillResult
 * @see Tool
 */
public interface Skill {

    /**
     * 获取技能名称
     * 
     * <p>名称是 Skill 的唯一标识符，用于 LLM 选择和调用。
     * 建议使用 snake_case 命名风格，如：query_highest_score_user</p>
     * 
     * @return 技能名称，不能为空
     */
    String getName();

    /**
     * 获取技能描述
     * 
     * <p>描述用于告知 LLM 该技能的功能和用途。
     * LLM 会根据描述来判断是否需要调用此技能。</p>
     * 
     * <p>示例："查询系统中积分最高的用户"</p>
     * 
     * @return 技能描述，建议 50-200 字符
     */
    String getDescription();

    /**
     * 获取所需工具列表
     * 
     * <p>返回该 Skill 执行时需要调用的所有 Tool 名称。
     * 用于：
     * <ul>
     *   <li>依赖检查：确保所需 Tool 已注册</li>
     *   <li>文档生成：告知使用者该 Skill 的依赖</li>
     * </ul></p>
     * 
     * @return 所需 Tool 名称列表
     */
    List<String> getRequiredTools();

    /**
     * 执行技能工作流
     * 
     * <p>这是 Skill 的核心方法，执行完整的业务流程。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 验证所需 Tool 是否可用
     * 2. 按业务逻辑顺序调用各个 Tool
     * 3. 收集每个 Tool 的执行结果
     * 4. 汇总结果并返回 SkillResult
     * </pre>
     * 
     * <h3>示例：</h3>
     * <pre>
     * public SkillResult execute(Map&lt;String, Object&gt; context) {
     *     // 步骤1：调用 Tool A 查询数据
     *     ToolResult queryResult = toolRegistry.getTool("query_database").execute(params);
     *     
     *     // 步骤2：调用 Tool B 发送邮件
     *     ToolResult emailResult = toolRegistry.getTool("send_email").execute(emailParams);
     *     
     *     // 步骤3：汇总结果
     *     return SkillResult.success(finalData, toolResults);
     * }
     * </pre>
     * 
     * @param context 执行上下文，包含用户输入和会话信息
     * @return 执行结果，包含成功/失败状态、数据和所有 Tool 的执行结果
     */
    SkillResult execute(Map<String, Object> context);
}
