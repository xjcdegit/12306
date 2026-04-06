package com.springai.skills;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Skills 框架启动类 - 应用程序入口
 * 
 * <p>SkillsApplication 是 Spring AI Skills 框架的主启动类，
 * 负责初始化 Spring 容器并启动应用程序。</p>
 * 
 * <h3>启动流程：</h3>
 * <pre>
 * 1. JVM 加载主类 SkillsApplication
 * 2. 调用 SpringApplication.run() 方法
 * 3. Spring Boot 开始启动流程：
 *    ├── 创建 ApplicationContext
 *    ├── 加载所有 @Configuration 配置类
 *    ├── 自动配置（AutoConfiguration）
 *    ├── 扫描并注册所有 Bean
 *    └── 启动内嵌 Tomcat 服务器
 * 4. 应用程序就绪，开始监听请求
 * </pre>
 * 
 * <h3>核心注解说明：</h3>
 * <ul>
 *   <li>@SpringBootApplication - Spring Boot 应用主注解，包含：
 *     <ul>
 *       <li>@SpringBootConfiguration - 标识为配置类</li>
 *       <li>@EnableAutoConfiguration - 启用自动配置</li>
 *       <li>@ComponentScan - 自动扫描组件</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * <h3>组件扫描范围：</h3>
 * <pre>
 * com.springai.skills
 * ├── agent/          - Agent 引擎组件
 * ├── config/         - 配置类
 * ├── controller/     - REST 控制器
 * ├── core/           - 核心接口
 * ├── mcp/            - MCP 协议实现
 * ├── registry/       - 注册中心
 * └── tools/          - 工具和技能实现
 *     ├── atomic/     - 原子工具
 *     └── skills/     - 技能工作流
 * </pre>
 * 
 * <h3>启动后可用的 API 端点：</h3>
 * <pre>
 * GET  http://localhost:8080/api/skills/list           - 获取所有 Skills
 * GET  http://localhost:8080/api/skills/tools/list     - 获取所有 Tools
 * GET  http://localhost:8080/api/skills/prompt         - 获取 Skills Prompt
 * POST http://localhost:8080/api/skills/execute/{name} - 执行指定 Skill
 * </pre>
 * 
 * @author SpringAI Skills Framework
 */
@SpringBootApplication
public class SkillsApplication {

    /**
     * 应用程序主入口方法
     * 
     * <p>这是 JVM 启动应用程序的入口点。</p>
     * 
     * <h3>执行流程：</h3>
     * <pre>
     * 1. 接收命令行参数 args
     * 2. 调用 SpringApplication.run()
     * 3. 创建 Spring 容器
     * 4. 加载所有配置和组件
     * 5. 启动内嵌服务器
     * 6. 返回 ApplicationContext
     * </pre>
     * 
     * @param args 命令行参数，可用于覆盖配置
     */
    public static void main(String[] args) {
        SpringApplication.run(SkillsApplication.class, args);
    }
}
