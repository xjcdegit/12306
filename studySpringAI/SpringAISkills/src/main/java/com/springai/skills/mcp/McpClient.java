package com.springai.skills.mcp;

import com.springai.skills.core.Tool;
import com.springai.skills.core.ToolResult;
import com.springai.skills.registry.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 客户端 - 模型上下文协议客户端实现
 * 
 * <p>McpClient 实现了 MCP（Model Context Protocol）协议的客户端部分，
 * 负责与 MCP Server 通信，提供工具发现和调用能力。</p>
 * 
 * <h3>MCP 协议核心方法：</h3>
 * <ul>
 *   <li>tools/list - 获取所有可用工具列表</li>
 *   <li>tools/call - 调用指定工具执行操作</li>
 * </ul>
 * 
 * <h3>工作流程：</h3>
 * <pre>
 * Agent 需要获取工具列表
 *     ↓
 * McpClient.toolsList()
 *     ↓
 * 从 ToolRegistry 获取所有 Tool
 *     ↓
 * 转换为 MCP 协议格式返回
 *     ↓
 * Agent 需要调用工具
 *     ↓
 * McpClient.toolsCall(name, args)
 *     ↓
 * 从 ToolRegistry 获取 Tool 实例
 *     ↓
 * 执行 Tool.execute()
 *     ↓
 * 返回结果
 * </pre>
 * 
 * <h3>与 MCP Server 的关系：</h3>
 * <pre>
 * ┌─────────────┐     MCP 协议     ┌─────────────┐
 * │ McpClient   │ ←──────────────→ │ McpServer   │
 * │ (Agent端)   │                  │ (工具提供者) │
 * └─────────────┘                  └─────────────┘
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see McpServer
 * @see ToolRegistry
 */
@Component
public class McpClient {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(McpClient.class);

    /**
     * 工具注册中心 - 用于获取 Tool 实例
     */
    private final ToolRegistry toolRegistry;

    /**
     * 构造函数 - 依赖注入
     * 
     * @param toolRegistry 工具注册中心
     */
    public McpClient(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    /**
     * 获取工具列表 - MCP tools/list 方法
     * 
     * <p>实现 MCP 协议的 tools/list 方法，返回所有可用工具的元数据。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 从 ToolRegistry 获取所有已注册的 Tool
     * 2. 将每个 Tool 转换为 MCP 协议格式的 Schema
     * 3. 聚合为 tools 数组返回
     * </pre>
     * 
     * <h3>返回格式（MCP 协议标准）：</h3>
     * <pre>
     * {
     *   "tools": [
     *     {
     *       "name": "query_database",
     *       "description": "查询数据库并返回结果",
     *       "inputSchema": {
     *         "type": "object",
     *         "properties": { ... },
     *         "required": [ ... ]
     *       }
     *     }
     *   ]
     * }
     * </pre>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * 阶段2: Agent 需要知道系统具备哪些能力
     *     ↓
     * 调用 toolsList() 获取工具列表
     *     ↓
     * 构建 Prompt 发送给 LLM
     * </pre>
     * 
     * @return MCP 协议格式的工具列表响应
     */
    public Map<String, Object> toolsList() {
        Map<String, Object> response = new HashMap<>();
        
        // 步骤1：获取所有已注册的 Tool
        Collection<Tool> tools = toolRegistry.getAllTools();
        
        // 步骤2：转换为 MCP 协议格式
        List<Map<String, Object>> toolsArray = tools.stream()
            .map(this::toolToSchema)
            .toList();

        // 步骤3：构建响应
        response.put("tools", toolsArray);
        return response;
    }

    /**
     * 调用工具 - MCP tools/call 方法
     * 
     * <p>实现 MCP 协议的 tools/call 方法，执行指定的工具操作。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 从 ToolRegistry 获取指定名称的 Tool
     * 2. 检查 Tool 是否存在
     * 3. 调用 Tool.execute() 执行操作
     * 4. 封装执行结果为 MCP 协议格式返回
     * </pre>
     * 
     * <h3>返回格式（成功）：</h3>
     * <pre>
     * {
     *   "result": { ... 工具执行结果 ... }
     * }
     * </pre>
     * 
     * <h3>返回格式（失败）：</h3>
     * <pre>
     * {
     *   "error": "错误描述信息"
     * }
     * </pre>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * 阶段5: LLM 返回 tool_calls 指令
     *     ↓
     * Agent 解析出工具名称和参数
     *     ↓
     * 调用 toolsCall(name, arguments)
     *     ↓
     * 执行工具并返回结果
     * </pre>
     * 
     * @param toolName 要调用的工具名称
     * @param arguments 工具参数，由 LLM 生成
     * @return MCP 协议格式的工具执行响应
     */
    public Map<String, Object> toolsCall(String toolName, Map<String, Object> arguments) {
        Map<String, Object> response = new HashMap<>();

        // 步骤1：获取 Tool 实例
        Tool tool = toolRegistry.getTool(toolName);
        
        // 步骤2：检查 Tool 是否存在
        if (tool == null) {
            response.put("error", "Tool not found: " + toolName);
            return response;
        }

        // 步骤3：记录调用日志
        log.info("MCP tools/call: {} with arguments: {}", toolName, arguments);

        // 步骤4：执行 Tool
        ToolResult result = tool.execute(arguments != null ? arguments : new HashMap<>());

        // 步骤5：封装响应
        if (result.isSuccess()) {
            response.put("result", result.getData());
        } else {
            response.put("error", result.getError());
        }

        return response;
    }

    /**
     * 将 Tool 转换为 MCP Schema 格式
     * 
     * <p>内部方法，将 Tool 实例转换为 MCP 协议要求的 Schema 格式。</p>
     * 
     * <h3>转换映射：</h3>
     * <pre>
     * Tool.getName()        → schema.name
     * Tool.getDescription() → schema.description
     * Tool.getParametersSchema() → schema.inputSchema
     * </pre>
     * 
     * @param tool 要转换的工具实例
     * @return MCP 协议格式的工具 Schema
     */
    private Map<String, Object> toolToSchema(Tool tool) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("name", tool.getName());
        schema.put("description", tool.getDescription());
        schema.put("inputSchema", tool.getParametersSchema());
        return schema;
    }
}
