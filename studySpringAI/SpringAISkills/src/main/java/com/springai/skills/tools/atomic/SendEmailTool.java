package com.springai.skills.tools.atomic;

import com.springai.skills.core.Tool;
import com.springai.skills.core.ToolResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件发送工具 - 原子 Tool 示例实现
 * 
 * <p>SendEmailTool 是一个原子级别的工具，负责发送邮件到指定收件人。
 * 它是构建通知类 Skill 的基础组件。</p>
 * 
 * <h3>IoC 设计说明：</h3>
 * <p>该类使用 @Component 注解，由 Spring 容器自动创建和管理。
 * 注册逻辑由 DemoToolsConfiguration 统一处理。</p>
 * 
 * @author SpringAI Skills Framework
 * @see Tool
 * @see ToolResult
 */
@Component
public class SendEmailTool implements Tool {

    @Override
    public String getName() {
        return "send_email";
    }

    @Override
    public String getDescription() {
        return "Send an email to a specified recipient";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> toProp = new HashMap<>();
        toProp.put("type", "string");
        toProp.put("description", "Recipient email address");
        properties.put("to", toProp);
        
        Map<String, Object> subjectProp = new HashMap<>();
        subjectProp.put("type", "string");
        subjectProp.put("description", "Email subject");
        properties.put("subject", subjectProp);
        
        Map<String, Object> contentProp = new HashMap<>();
        contentProp.put("type", "string");
        contentProp.put("description", "Email content");
        properties.put("content", contentProp);
        
        schema.put("properties", properties);
        schema.put("required", java.util.List.of("to", "subject", "content"));
        return schema;
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        String to = (String) parameters.get("to");
        String subject = (String) parameters.get("subject");
        String content = (String) parameters.get("content");

        if (to == null || subject == null || content == null) {
            return ToolResult.failure("Missing required parameters: to, subject, content");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("to", to);
        result.put("subject", subject);
        result.put("status", "sent");
        result.put("messageId", "msg_" + System.currentTimeMillis());

        return ToolResult.success(result);
    }
}
