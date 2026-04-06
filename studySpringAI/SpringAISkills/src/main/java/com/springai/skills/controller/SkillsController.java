package com.springai.skills.controller;

import com.springai.skills.agent.AgentEngine;
import com.springai.skills.core.SkillResult;
import com.springai.skills.core.ToolResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 技能管理控制器 - REST API 入口
 * 
 * <p>SkillsController 提供了管理 Skill 和 Tool 的 REST API 端点，
 * 是外部系统与 Skill 框架交互的主要入口。</p>
 * 
 * <h3>API 端点列表：</h3>
 * <pre>
 * GET  /api/skills/list              - 获取所有可用 Skills 元数据
 * GET  /api/skills/tools/list        - 获取所有可用 Tools 元数据
 * GET  /api/skills/prompt            - 获取 Skills Prompt（供 LLM 使用）
 * GET  /api/skills/tools/prompt      - 获取 Tools Prompt（供 LLM 使用）
 * POST /api/skills/execute/{name}    - 执行指定 Skill
 * POST /api/skills/tools/execute/{name} - 执行指定 Tool
 * </pre>
 * 
 * <h3>典型使用流程：</h3>
 * <pre>
 * 1. 调用 GET /api/skills/list 获取可用 Skills
 * 2. 将 Skills 元数据构建成 Prompt 发送给 LLM
 * 3. LLM 返回要执行的 Skill 名称
 * 4. 调用 POST /api/skills/execute/{name} 执行 Skill
 * 5. 返回执行结果给用户
 * </pre>
 * 
 * <h3>架构位置：</h3>
 * <pre>
 * ┌─────────────────┐
 * │   外部客户端     │
 * └────────┬────────┘
 *          │ HTTP 请求
 *          ▼
 * ┌─────────────────┐
 * │ SkillsController │ ← 当前类
 * └────────┬────────┘
 *          │ 调用
 *          ▼
 * ┌─────────────────┐
 * │   AgentEngine   │
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │  Skill/Tool     │
 * └─────────────────┘
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see AgentEngine
 * @see SkillResult
 * @see ToolResult
 */
@RestController
@RequestMapping("/api/skills")
public class SkillsController {

    /**
     * Agent 引擎 - 核心调度组件
     */
    private final AgentEngine agentEngine;

    /**
     * 构造函数 - 依赖注入
     * 
     * @param agentEngine Agent 引擎实例
     */
    public SkillsController(AgentEngine agentEngine) {
        this.agentEngine = agentEngine;
    }

    /**
     * 获取所有可用 Skills 元数据
     * 
     * <p>返回系统中所有已注册 Skill 的元数据信息，
     * 包括名称、描述、所需工具等。</p>
     * 
     * <h3>返回格式：</h3>
     * <pre>
     * {
     *   "skills": [
     *     {
     *       "name": "query_highest_score_user",
     *       "description": "Query the user with the highest score",
     *       "requiredTools": ["query_database"]
     *     }
     *   ]
     * }
     * </pre>
     * 
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>前端展示可用技能列表</li>
     *   <li>构建 LLM Prompt</li>
     *   <li>技能发现和选择</li>
     * </ul>
     * 
     * @return Skills 元数据 Map
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listSkills() {
        return ResponseEntity.ok(agentEngine.getSkillsMetadata());
    }

    /**
     * 获取所有可用 Tools 元数据
     * 
     * <p>返回系统中所有已注册 Tool 的元数据信息，
     * 包括名称、描述、参数 Schema 等。</p>
     * 
     * <h3>返回格式：</h3>
     * <pre>
     * {
     *   "tools": [
     *     {
     *       "name": "query_database",
     *       "description": "Execute a SQL query",
     *       "inputSchema": { ... }
     *     }
     *   ]
     * }
     * </pre>
     * 
     * @return Tools 元数据 Map
     */
    @GetMapping("/tools/list")
    public ResponseEntity<Map<String, Object>> listTools() {
        return ResponseEntity.ok(agentEngine.getToolsMetadata());
    }

    /**
     * 获取 Skills Prompt
     * 
     * <p>生成供 LLM 使用的 Skills 描述 Prompt，
     * 包含所有可用 Skill 的信息，帮助 LLM 选择合适的 Skill。</p>
     * 
     * <h3>生成流程：</h3>
     * <pre>
     * 1. 从 SkillRegistry 获取所有 Skill
     * 2. 遍历每个 Skill，提取名称和描述
     * 3. 格式化为 Prompt 文本
     * 4. 返回给调用方
     * </pre>
     * 
     * <h3>返回示例：</h3>
     * <pre>
     * Available Skills:
     * 1. query_highest_score_user: Query the user with the highest score
     * 2. send_congratulation_email: Query highest score user and send email
     * </pre>
     * 
     * @return Skills Prompt 字符串
     */
    @GetMapping("/prompt")
    public ResponseEntity<String> getSkillsPrompt() {
        return ResponseEntity.ok(agentEngine.buildSkillsPrompt());
    }

    /**
     * 获取 Tools Prompt
     * 
     * <p>生成供 LLM 使用的 Tools 描述 Prompt，
     * 包含所有可用 Tool 的信息，帮助 LLM 选择和调用 Tool。</p>
     * 
     * @return Tools Prompt 字符串
     */
    @GetMapping("/tools/prompt")
    public ResponseEntity<String> getToolsPrompt() {
        return ResponseEntity.ok(agentEngine.buildToolsPrompt());
    }

    /**
     * 执行指定 Skill
     * 
     * <p>根据名称执行对应的 Skill 工作流。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 接收 Skill 名称和可选上下文
     * 2. 调用 AgentEngine.executeSkill()
     * 3. AgentEngine 从 SkillRegistry 获取 Skill
     * 4. 执行 Skill.execute()
     * 5. Skill 内部调用多个 Tool
     * 6. 返回 SkillResult
     * </pre>
     * 
     * <h3>请求示例：</h3>
     * <pre>
     * POST /api/skills/execute/query_highest_score_user
     * Content-Type: application/json
     * 
     * {
     *   "userId": "123",
     *   "locale": "zh-CN"
     * }
     * </pre>
     * 
     * <h3>响应示例：</h3>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "highestScoreUser": { ... }
     *   },
     *   "toolResults": { ... }
     * }
     * </pre>
     * 
     * @param skillName Skill 名称
     * @param context 执行上下文（可选）
     * @return Skill 执行结果
     */
    @PostMapping("/execute/{skillName}")
    public ResponseEntity<SkillResult> executeSkill(
            @PathVariable String skillName,
            @RequestBody(required = false) Map<String, Object> context) {
        SkillResult result = agentEngine.executeSkill(skillName, context != null ? context : Map.of());
        return ResponseEntity.ok(result);
    }

    /**
     * 执行指定 Tool
     * 
     * <p>根据名称执行对应的原子 Tool 操作。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 接收 Tool 名称和参数
     * 2. 调用 AgentEngine.executeTool()
     * 3. AgentEngine 从 ToolRegistry 获取 Tool
     * 4. 执行 Tool.execute()
     * 5. 返回 ToolResult
     * </pre>
     * 
     * <h3>请求示例：</h3>
     * <pre>
     * POST /api/skills/tools/execute/query_database
     * Content-Type: application/json
     * 
     * {
     *   "query": "SELECT * FROM users LIMIT 10"
     * }
     * </pre>
     * 
     * <h3>响应示例：</h3>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "rows": 10,
     *     "data": [ ... ]
     *   }
     * }
     * </pre>
     * 
     * @param toolName Tool 名称
     * @param parameters 执行参数（可选）
     * @return Tool 执行结果
     */
    @PostMapping("/tools/execute/{toolName}")
    public ResponseEntity<ToolResult> executeTool(
            @PathVariable String toolName,
            @RequestBody(required = false) Map<String, Object> parameters) {
        ToolResult result = agentEngine.executeTool(toolName, parameters != null ? parameters : Map.of());
        return ResponseEntity.ok(result);
    }
}
