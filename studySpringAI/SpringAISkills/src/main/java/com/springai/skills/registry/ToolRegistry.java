package com.springai.skills.registry;

import com.springai.skills.core.Tool;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心 - 管理所有 Tool 的注册与查找
 * 
 * <p>ToolRegistry 是系统中所有 Tool 的统一管理中心，负责：
 * <ul>
 *   <li>Tool 的注册与注销</li>
 *   <li>Tool 的查找与获取</li>
 *   <li>Tool 元数据的聚合</li>
 * </ul></p>
 * 
 * <h3>工作流程：</h3>
 * <pre>
 * 系统启动
 *     ↓
 * 扫描并注册所有 Tool Bean
 *     ↓
 * Agent 请求 tools/list
 *     ↓
 * ToolRegistry 返回所有已注册的 Tool
 *     ↓
 * LLM 选择合适的 Tool
 *     ↓
 * Agent 通过 ToolRegistry 获取 Tool 实例
 *     ↓
 * 执行 Tool
 * </pre>
 * 
 * <h3>线程安全：</h3>
 * <p>使用 ConcurrentHashMap 存储工具，支持并发访问。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * // 注册工具
 * toolRegistry.register(new QueryDatabaseTool());
 * 
 * // 获取工具
 * Tool tool = toolRegistry.getTool("query_database");
 * 
 * // 检查工具是否存在
 * if (toolRegistry.hasTool("query_database")) {
 *     tool.execute(parameters);
 * }
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see Tool
 * @see SkillRegistry
 */
@Component
public class ToolRegistry {

    /**
     * 工具存储映射
     * Key: Tool 名称（由 Tool.getName() 返回）
     * Value: Tool 实例
     * 
     * 使用 ConcurrentHashMap 保证线程安全，支持并发注册和查询
     */
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    /**
     * 注册工具
     * 
     * <p>将 Tool 实例注册到注册中心，使其可被 Agent 发现和调用。</p>
     * 
     * <h3>注册流程：</h3>
     * <pre>
     * 1. 获取 Tool 的名称（通过 getName()）
     * 2. 以名称为 Key 存储到 tools Map
     * 3. 如果已存在同名工具，将被覆盖
     * </pre>
     * 
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>Tool 名称必须唯一，重复注册会覆盖之前的工具</li>
     *   <li>建议在系统启动时完成所有注册</li>
     * </ul>
     * 
     * @param tool 要注册的工具实例
     */
    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * 注销工具
     * 
     * <p>从注册中心移除指定名称的工具。</p>
     * 
     * <p>使用场景：动态卸载工具、热更新工具实现。</p>
     * 
     * @param name 要注销的工具名称
     */
    public void unregister(String name) {
        tools.remove(name);
    }

    /**
     * 获取工具
     * 
     * <p>根据名称获取已注册的工具实例。</p>
     * 
     * <h3>调用流程：</h3>
     * <pre>
     * Agent 收到 LLM 的 tool_calls 指令
     *     ↓
     * 解析出工具名称
     *     ↓
     * 调用 getTool(name) 获取工具实例
     *     ↓
     * 执行工具的 execute 方法
     * </pre>
     * 
     * @param name 工具名称
     * @return 工具实例，如果不存在则返回 null
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }

    /**
     * 获取所有已注册的工具
     * 
     * <p>返回所有已注册工具的集合，用于：
     * <ul>
     *   <li>构建 LLM 的 tools 数组</li>
     *   <li>生成工具列表 API 响应</li>
     *   <li>工具健康检查</li>
     * </ul></p>
     * 
     * @return 所有工具实例的集合
     */
    public Collection<Tool> getAllTools() {
        return tools.values();
    }

    /**
     * 检查工具是否存在
     * 
     * <p>判断指定名称的工具是否已注册。</p>
     * 
     * <p>使用场景：在执行 Skill 前，验证其依赖的工具是否可用。</p>
     * 
     * @param name 工具名称
     * @return true 表示存在，false 表示不存在
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * 获取已注册工具数量
     * 
     * @return 已注册工具的数量
     */
    public int size() {
        return tools.size();
    }
}
