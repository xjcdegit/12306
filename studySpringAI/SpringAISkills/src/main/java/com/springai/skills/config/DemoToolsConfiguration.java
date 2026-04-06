package com.springai.skills.config;

import com.springai.skills.core.Skill;
import com.springai.skills.core.Tool;
import com.springai.skills.registry.SkillRegistry;
import com.springai.skills.registry.ToolRegistry;
import com.springai.skills.tools.atomic.QueryDatabaseTool;
import com.springai.skills.tools.atomic.SendEmailTool;
import com.springai.skills.tools.skills.QueryHighestScoreUserSkill;
import com.springai.skills.tools.skills.SendCongratulationEmailSkill;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 演示工具配置类 - 统一管理 Tool 和 Skill 的注册
 * 
 * <p>DemoToolsConfiguration 负责将 Spring 容器创建的 Tool 和 Skill Bean
 * 注册到对应的 Registry 中。</p>
 * 
 * <h3>IoC 设计说明：</h3>
 * <p>所有 Tool 和 Skill Bean 由 Spring 容器通过 @Component 自动创建，
 * 本配置类通过依赖注入获取这些 Bean 并注册到 Registry。</p>
 * 
 * <h3>注册流程：</h3>
 * <pre>
 * 1. Spring 容器扫描 @Component 注解的类
 * 2. Spring 自动创建 Tool 和 Skill Bean（IoC）
 * 3. 本配置类通过构造函数注入获取这些 Bean
 * 4. @PostConstruct 方法中将 Bean 注册到 Registry
 * </pre>
 * 
 * @author SpringAI Skills Framework
 * @see ToolRegistry
 * @see SkillRegistry
 */
@Configuration
@ConditionalOnProperty(name = "spring-ai-skills.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoToolsConfiguration {

    private final ToolRegistry toolRegistry;
    private final SkillRegistry skillRegistry;
    
    private final QueryDatabaseTool queryDatabaseTool;
    private final SendEmailTool sendEmailTool;
    private final QueryHighestScoreUserSkill queryHighestScoreUserSkill;
    private final SendCongratulationEmailSkill sendCongratulationEmailSkill;

    public DemoToolsConfiguration(
            ToolRegistry toolRegistry,
            SkillRegistry skillRegistry,
            QueryDatabaseTool queryDatabaseTool,
            SendEmailTool sendEmailTool,
            QueryHighestScoreUserSkill queryHighestScoreUserSkill,
            SendCongratulationEmailSkill sendCongratulationEmailSkill) {
        this.toolRegistry = toolRegistry;
        this.skillRegistry = skillRegistry;
        this.queryDatabaseTool = queryDatabaseTool;
        this.sendEmailTool = sendEmailTool;
        this.queryHighestScoreUserSkill = queryHighestScoreUserSkill;
        this.sendCongratulationEmailSkill = sendCongratulationEmailSkill;
        
        registerAll();
    }

    private void registerAll() {
        toolRegistry.register(queryDatabaseTool);
        toolRegistry.register(sendEmailTool);
        skillRegistry.register(queryHighestScoreUserSkill);
        skillRegistry.register(sendCongratulationEmailSkill);
    }
}
