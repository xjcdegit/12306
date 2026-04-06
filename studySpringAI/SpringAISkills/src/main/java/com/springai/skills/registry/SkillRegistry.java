package com.springai.skills.registry;

import com.springai.skills.core.Skill;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 技能注册中心 - 管理所有 Skill 的注册与查找
 * 
 * <p>SkillRegistry 是系统中所有 Skill 的统一管理中心，负责：
 * <ul>
 *   <li>Skill 的注册与注销</li>
 *   <li>Skill 的查找与获取</li>
 *   <li>Skill 元数据的聚合</li>
 * </ul></p>
 * 
 * <h3>工作流程：</h3>
 * <pre>
 * 系统启动
 *     ↓
 * 扫描并注册所有 Skill Bean
 *     ↓
 * Agent 构建 Skills 列表 Prompt
 *     ↓
 * LLM 根据用户意图选择 Skill
 *     ↓
 * Agent 通过 SkillRegistry 获取 Skill 实例
 *     ↓
 * 执行 Skill 的工作流
 * </pre>
 * 
 * <h3>与 ToolRegistry 的关系：</h3>
 * <pre>
 * SkillRegistry 管理 Skill（工作流）
 *     ↓
 * Skill 内部依赖多个 Tool
 *     ↓
 * ToolRegistry 管理 Tool（原子操作）
 * </pre>
 * 
 * <h3>线程安全：</h3>
 * <p>使用 ConcurrentHashMap 存储技能，支持并发访问。</p>
 * 
 * @author SpringAI Skills Framework
 * @see Skill
 * @see ToolRegistry
 */
@Component
public class SkillRegistry {

    /**
     * 技能存储映射
     * Key: Skill 名称（由 Skill.getName() 返回）
     * Value: Skill 实例
     * 
     * 使用 ConcurrentHashMap 保证线程安全，支持并发注册和查询
     */
    private final Map<String, Skill> skills = new ConcurrentHashMap<>();

    /**
     * 注册技能
     * 
     * <p>将 Skill 实例注册到注册中心，使其可被 Agent 发现和调用。</p>
     * 
     * <h3>注册流程：</h3>
     * <pre>
     * 1. 获取 Skill 的名称（通过 getName()）
     * 2. 以名称为 Key 存储到 skills Map
     * 3. 如果已存在同名技能，将被覆盖
     * </pre>
     * 
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>Skill 名称必须唯一，重复注册会覆盖之前的技能</li>
     *   <li>注册前应确保 Skill 依赖的 Tool 已注册</li>
     * </ul>
     * 
     * @param skill 要注册的技能实例
     */
    public void register(Skill skill) {
        skills.put(skill.getName(), skill);
    }

    /**
     * 注销技能
     * 
     * <p>从注册中心移除指定名称的技能。</p>
     * 
     * <p>使用场景：动态卸载技能、热更新技能实现。</p>
     * 
     * @param name 要注销的技能名称
     */
    public void unregister(String name) {
        skills.remove(name);
    }

    /**
     * 获取技能
     * 
     * <p>根据名称获取已注册的技能实例。</p>
     * 
     * <h3>调用流程：</h3>
     * <pre>
     * LLM 分析用户意图，选择合适的 Skill
     *     ↓
     * 返回 Skill 名称
     *     ↓
     * Agent 调用 getSkill(name) 获取技能实例
     *     ↓
     * 执行技能的 execute 方法
     * </pre>
     * 
     * @param name 技能名称
     * @return 技能实例，如果不存在则返回 null
     */
    public Skill getSkill(String name) {
        return skills.get(name);
    }

    /**
     * 获取所有已注册的技能
     * 
     * <p>返回所有已注册技能的集合，用于：
     * <ul>
     *   <li>构建 LLM 的 Skills 描述 Prompt</li>
     *   <li>生成技能列表 API 响应</li>
     *   <li>技能健康检查</li>
     * </ul></p>
     * 
     * @return 所有技能实例的集合
     */
    public Collection<Skill> getAllSkills() {
        return skills.values();
    }

    /**
     * 检查技能是否存在
     * 
     * <p>判断指定名称的技能是否已注册。</p>
     * 
     * <p>使用场景：在执行前验证技能是否可用。</p>
     * 
     * @param name 技能名称
     * @return true 表示存在，false 表示不存在
     */
    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }

    /**
     * 获取已注册技能数量
     * 
     * @return 已注册技能的数量
     */
    public int size() {
        return skills.size();
    }
}
