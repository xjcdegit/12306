package com.springai.skills.agent;

import com.springai.skills.core.Skill;
import com.springai.skills.core.SkillResult;
import com.springai.skills.core.Tool;
import com.springai.skills.core.ToolResult;
import com.springai.skills.mcp.McpClient;
import com.springai.skills.registry.SkillRegistry;
import com.springai.skills.registry.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 引擎 - 系统核心调度器
 * 
 * <p>AgentEngine 是整个系统的核心调度组件，负责协调 LLM、Skill 和 Tool 之间的交互。
 * 它是连接用户请求和系统能力的桥梁。</p>
 * 
 * <h3>核心职责：</h3>
 * <ul>
 *   <li>管理 Skill 和 Tool 的发现与获取</li>
 *   <li>执行 Skill 工作流和 Tool 操作</li>
 *   <li>构建 LLM 所需的 Prompt 和元数据</li>
 *   <li>协调 MCP 协议调用</li>
 * </ul>
 * 
 * <h3>完整工作流程：</h3>
 * <pre>
 * 阶段1: 接收用户输入
 *     ↓
 * 阶段2: 获取可用 Skills 列表（getAvailableSkills）
 *     ↓
 * 阶段3: 构建 Prompt 发送给 LLM（buildSkillsPrompt）
 *     ↓
 * 阶段4: LLM 决策，返回要调用的 Skill 名称
 *     ↓
 * 阶段5: 执行 Skill（executeSkill）
 *     ↓
 * 阶段6: Skill 内部调用多个 Tool（executeTool）
 *     ↓
 * 阶段7: 结果回传 LLM，生成最终响应
 * </pre>
 * 
 * <h3>与其他组件的关系：</h3>
 * <pre>
 *                    ┌─────────────┐
 *                    │   用户请求   │
 *                    └──────┬──────┘
 *                           ↓
 *                    ┌─────────────┐
 *                    │ AgentEngine │ ← 核心调度器
 *                    └──────┬──────┘
 *           ┌───────────────┼───────────────┐
 *           ↓               ↓               ↓
 *    ┌────────────┐  ┌────────────┐  ┌────────────┐
 *    │SkillRegistry│  │ToolRegistry│  │  McpClient │
 *    └────────────┘  └────────────┘  └────────────┘
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see SkillRegistry
 * @see ToolRegistry
 * @see McpClient
 */
@Component
public class AgentEngine {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(AgentEngine.class);

    /**
     * 工具注册中心 - 用于获取和管理 Tool
     */
    private final ToolRegistry toolRegistry;

    /**
     * 技能注册中心 - 用于获取和管理 Skill
     */
    private final SkillRegistry skillRegistry;

    /**
     * MCP 客户端 - 用于协议级别的工具调用
     */
    private final McpClient mcpClient;

    /**
     * 构造函数 - 依赖注入
     * 
     * @param toolRegistry 工具注册中心
     * @param skillRegistry 技能注册中心
     * @param mcpClient MCP 客户端
     */
    public AgentEngine(ToolRegistry toolRegistry, SkillRegistry skillRegistry, McpClient mcpClient) {
        this.toolRegistry = toolRegistry;
        this.skillRegistry = skillRegistry;
        this.mcpClient = mcpClient;
    }

    /**
     * 获取所有可用技能
     * 
     * <p>返回系统中已注册的所有 Skill 列表，用于：
     * <ul>
     *   <li>构建 LLM 的 Skills 描述 Prompt</li>
     *   <li>API 接口返回技能列表</li>
     * </ul></p>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * 用户发送请求 → Agent 构建 Prompt → 调用此方法获取 Skills 列表
     * </pre>
     * 
     * @return 不可变的 Skill 列表
     */
    public List<Skill> getAvailableSkills() {
        return List.copyOf(skillRegistry.getAllSkills());
    }

    /**
     * 获取所有可用工具
     * 
     * <p>返回系统中已注册的所有 Tool 列表，用于：
     * <ul>
     *   <li>构建 LLM 的 Tools 描述 Prompt</li>
     *   <li>API 接口返回工具列表</li>
     * </ul></p>
     * 
     * @return 不可变的 Tool 列表
     */
    public List<Tool> getAvailableTools() {
        return List.copyOf(toolRegistry.getAllTools());
    }

    /**
     * 执行技能工作流
     * 
     * <p>根据技能名称执行对应的工作流。这是 Agent 调用 Skill 的核心入口。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 从 SkillRegistry 获取 Skill 实例
     * 2. 检查 Skill 是否存在
     * 3. 调用 Skill.execute() 执行工作流
     * 4. 返回执行结果
     * </pre>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * LLM 返回要调用的 Skill 名称 → Agent 调用此方法执行 Skill
     * </pre>
     * 
     * @param skillName 技能名称
     * @param context 执行上下文，包含用户输入和会话信息
     * @return 技能执行结果
     */
    public SkillResult executeSkill(String skillName, Map<String, Object> context) {
        // 步骤1：从注册中心获取 Skill
        Skill skill = skillRegistry.getSkill(skillName);
        
        // 步骤2：检查 Skill 是否存在
        if (skill == null) {
            return SkillResult.failure("Skill not found: " + skillName);
        }

        // 步骤3：记录执行日志
        log.info("Executing skill: {}", skillName);
        
        // 步骤4：执行 Skill 工作流
        return skill.execute(context);
    }

    /**
     * 执行工具操作
     * 
     * <p>根据工具名称执行对应的原子操作。这是 Agent 调用 Tool 的核心入口。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 从 ToolRegistry 获取 Tool 实例
     * 2. 检查 Tool 是否存在
     * 3. 调用 Tool.execute() 执行操作
     * 4. 返回执行结果
     * </pre>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * Skill 内部需要调用 Tool → 通过此方法执行
     * 或
     * LLM 直接调用 Tool（不经过 Skill）
     * </pre>
     * 
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    public ToolResult executeTool(String toolName, Map<String, Object> parameters) {
        // 步骤1：从注册中心获取 Tool
        Tool tool = toolRegistry.getTool(toolName);
        
        // 步骤2：检查 Tool 是否存在
        if (tool == null) {
            return ToolResult.failure("Tool not found: " + toolName);
        }

        // 步骤3：记录执行日志
        log.info("Executing tool: {} with parameters: {}", toolName, parameters);
        
        // 步骤4：执行 Tool 操作
        return tool.execute(parameters);
    }

    /**
     * 构建技能描述 Prompt
     * 
     * <p>生成用于 LLM 的 Skills 描述文本，告知 LLM 当前系统有哪些技能可用。</p>
     * 
     * <h3>输出格式：</h3>
     * <pre>
     * Available Skills:
     * - query_highest_score_user: 查询系统中积分最高的用户
     * - send_congratulation_email: 查询最高分用户并发送祝贺邮件
     * </pre>
     * 
     * <h3>使用场景：</h3>
     * <pre>
     * 构建 System Prompt → 添加此描述 → 发送给 LLM
     * </pre>
     * 
     * @return Skills 描述文本
     */
    public String buildSkillsPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available Skills:\n");

        // 遍历所有 Skill，构建描述
        for (Skill skill : skillRegistry.getAllSkills()) {
            sb.append("- ").append(skill.getName())
              .append(": ").append(skill.getDescription())
              .append("\n");
        }

        return sb.toString();
    }

    /**
     * 构建工具描述 Prompt
     * 
     * <p>生成用于 LLM 的 Tools 描述文本，告知 LLM 当前系统有哪些工具可用。</p>
     * 
     * <h3>输出格式：</h3>
     * <pre>
     * Available Tools:
     * - query_database: 查询数据库并返回结果
     * - send_email: 发送邮件到指定收件人
     * </pre>
     * 
     * @return Tools 描述文本
     */
    public String buildToolsPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available Tools:\n");

        // 遍历所有 Tool，构建描述
        for (Tool tool : toolRegistry.getAllTools()) {
            sb.append("- ").append(tool.getName())
              .append(": ").append(tool.getDescription())
              .append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取技能元数据
     * 
     * <p>返回所有 Skill 的详细元数据，用于 API 响应。</p>
     * 
     * <h3>返回格式：</h3>
     * <pre>
     * {
     *   "query_highest_score_user": {
     *     "name": "query_highest_score_user",
     *     "description": "查询系统中积分最高的用户",
     *     "requiredTools": ["query_database"]
     *   }
     * }
     * </pre>
     * 
     * @return 技能元数据映射
     */
    public Map<String, Object> getSkillsMetadata() {
        Map<String, Object> result = new HashMap<>();
        
        // 遍历所有 Skill，构建元数据
        for (Skill skill : skillRegistry.getAllSkills()) {
            Map<String, Object> skillInfo = new HashMap<>();
            skillInfo.put("name", skill.getName());
            skillInfo.put("description", skill.getDescription());
            skillInfo.put("requiredTools", skill.getRequiredTools());
            result.put(skill.getName(), skillInfo);
        }

        return result;
    }

    /**
     * 获取工具元数据
     * 
     * <p>返回所有 Tool 的详细元数据，用于 API 响应。</p>
     * 
     * <h3>返回格式：</h3>
     * <pre>
     * {
     *   "query_database": {
     *     "name": "query_database",
     *     "description": "查询数据库并返回结果",
     *     "parametersSchema": { ... }
     *   }
     * }
     * </pre>
     * 
     * @return 工具元数据映射
     */
    public Map<String, Object> getToolsMetadata() {
        Map<String, Object> result = new HashMap<>();

        // 遍历所有 Tool，构建元数据
        for (Tool tool : toolRegistry.getAllTools()) {
            Map<String, Object> toolInfo = new HashMap<>();
            toolInfo.put("name", tool.getName());
            toolInfo.put("description", tool.getDescription());
            toolInfo.put("parametersSchema", tool.getParametersSchema());
            result.put(tool.getName(), toolInfo);
        }

        return result;
    }
}
