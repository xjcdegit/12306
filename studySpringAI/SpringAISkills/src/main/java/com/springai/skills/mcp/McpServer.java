package com.springai.skills.mcp;

import com.springai.skills.core.Tool;
import com.springai.skills.core.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * MCP 服务端接口 - 工具提供者抽象
 * 
 * <p>McpServer 定义了 MCP（Model Context Protocol）协议的服务端接口。
 * 实现 this 接口的类可以作为工具提供者，向 McpClient 暴露工具能力。</p>
 * 
 * <h3>MCP 协议架构：</h3>
 * <pre>
 * ┌─────────────────────────────────────────────────────┐
 * │                    Agent 应用层                      │
 * └─────────────────────────────────────────────────────┘
 *                          ↑↓
 * ┌─────────────────────────────────────────────────────┐
 * │                 MCP 协议层                           │
 * │  ┌─────────────┐              ┌─────────────┐       │
 * │  │ McpClient   │ ←── 通信 ──→ │ McpServer   │       │
 * │  │ (客户端)    │              │ (服务端)    │       │
 * │  └─────────────┘              └─────────────┘       │
 * └─────────────────────────────────────────────────────┘
 *                          ↓
 * ┌─────────────────────────────────────────────────────┐
 * │                 工具实现层                           │
 * │  ┌──────────┐ ┌──────────┐ ┌──────────┐            │
 * │  │ Tool A   │ │ Tool B   │ │ Tool C   │            │
 * │  └──────────┘ └──────────┘ └──────────┘            │
 * └─────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>实现示例：</h3>
 * <pre>
 * public class DatabaseMcpServer implements McpServer {
 *     
 *     private List&lt;Tool&gt; tools;
 *     
 *     public String getServerName() {
 *         return "database-server";
 *     }
 *     
 *     public List&lt;Tool&gt; getTools() {
 *         return tools;
 *     }
 *     
 *     public ToolResult executeTool(String toolName, Map&lt;String, Object&gt; params) {
 *         Tool tool = findTool(toolName);
 *         return tool.execute(params);
 *     }
 * }
 * </pre>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>本地工具服务：直接在 JVM 内提供工具</li>
 *   <li>远程工具服务：通过网络暴露工具能力</li>
 *   <li>插件式工具：动态加载/卸载工具模块</li>
 * </ul>
 * 
 * @author SpringAI Skills Framework
 * @see McpClient
 * @see Tool
 */
public interface McpServer {

    /**
     * 获取服务端名称
     * 
     * <p>返回 MCP Server 的唯一标识名称，用于：
     * <ul>
     *   <li>日志记录和调试</li>
     *   <li>多 Server 场景下的路由区分</li>
     *   <li>健康检查和监控</li>
     * </ul></p>
     * 
     * <p>建议使用 kebab-case 命名风格，如：database-server、email-server</p>
     * 
     * @return 服务端名称
     */
    String getServerName();

    /**
     * 获取服务端提供的所有工具
     * 
     * <p>返回该 Server 提供的所有 Tool 实例列表。</p>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * McpClient 发起 tools/list 请求
     *     ↓
     * 遍历所有 McpServer
     *     ↓
     * 调用 getTools() 获取工具列表
     *     ↓
     * 聚合返回给 Agent
     * </pre>
     * 
     * @return 工具列表
     */
    List<Tool> getTools();

    /**
     * 执行工具操作
     * 
     * <p>根据工具名称执行对应的工具操作。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 根据工具名称查找对应的 Tool 实例
     * 2. 调用 Tool.execute(parameters)
     * 3. 返回执行结果
     * </pre>
     * 
     * <h3>调用时机：</h3>
     * <pre>
     * McpClient 发起 tools/call 请求
     *     ↓
     * 路由到对应的 McpServer
     *     ↓
     * 调用 executeTool(toolName, parameters)
     *     ↓
     * 返回结果给 McpClient
     * </pre>
     * 
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    ToolResult executeTool(String toolName, Map<String, Object> parameters);

    /**
     * 启动服务端
     * 
     * <p>初始化并启动 MCP Server，使其可以接收和处理请求。</p>
     * 
     * <h3>启动流程：</h3>
     * <pre>
     * 1. 初始化资源（如数据库连接、网络端口）
     * 2. 注册工具到内部映射
     * 3. 开始监听请求
     * </pre>
     */
    void start();

    /**
     * 停止服务端
     * 
     * <p>优雅关闭 MCP Server，释放所有资源。</p>
     * 
     * <h3>停止流程：</h3>
     * <pre>
     * 1. 停止接收新请求
     * 2. 等待进行中的请求完成
     * 3. 释放资源（如关闭连接、释放端口）
     * </pre>
     */
    void stop();
}
