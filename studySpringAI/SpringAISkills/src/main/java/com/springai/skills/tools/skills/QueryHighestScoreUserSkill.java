package com.springai.skills.tools.skills;

import com.springai.skills.core.Skill;
import com.springai.skills.core.SkillResult;
import com.springai.skills.core.Tool;
import com.springai.skills.core.ToolResult;
import com.springai.skills.registry.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询最高分用户技能 - Skill 示例实现
 * 
 * <p>QueryHighestScoreUserSkill 是一个简单的工作流 Skill，
 * 封装了"查询系统中积分最高用户"的完整业务流程。</p>
 * 
 * <h3>IoC 设计说明：</h3>
 * <p>该类使用 @Component 注解，由 Spring 容器自动创建和管理。
 * ToolRegistry 通过构造函数注入，注册逻辑由 DemoToolsConfiguration 统一处理。</p>
 * 
 * @author SpringAI Skills Framework
 * @see Skill
 * @see SkillResult
 */
@Component
public class QueryHighestScoreUserSkill implements Skill {

    private final ToolRegistry toolRegistry;

    public QueryHighestScoreUserSkill(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public String getName() {
        return "query_highest_score_user";
    }

    @Override
    public String getDescription() {
        return "Query the user with the highest score in the system";
    }

    @Override
    public List<String> getRequiredTools() {
        return List.of("query_database");
    }

    @Override
    public SkillResult execute(Map<String, Object> context) {
        Map<String, ToolResult> toolResults = new HashMap<>();

        Tool queryTool = toolRegistry.getTool("query_database");
        if (queryTool == null) {
            return SkillResult.failure("Required tool 'query_database' not found");
        }

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", "SELECT * FROM users ORDER BY score DESC LIMIT 1");

        ToolResult queryResult = queryTool.execute(queryParams);
        toolResults.put("query_database", queryResult);

        if (!queryResult.isSuccess()) {
            return SkillResult.failure("Failed to query database: " + queryResult.getError());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("highestScoreUser", queryResult.getData());
        result.put("toolResults", toolResults);

        return SkillResult.success(result, toolResults);
    }
}
