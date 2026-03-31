package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件工具
 * 提供邮件发送功能（模拟）
 */
@Component
public class EmailTool implements Tool {

    @Override
    public String getName() {
        return "email";
    }

    @Override
    public String getDescription() {
        return "发送邮件到指定邮箱地址";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> to = new HashMap<>();
        to.put("type", "string");
        to.put("description", "收件人邮箱地址");
        properties.put("to", to);
        
        Map<String, Object> subject = new HashMap<>();
        subject.put("type", "string");
        subject.put("description", "邮件主题");
        properties.put("subject", subject);
        
        Map<String, Object> body = new HashMap<>();
        body.put("type", "string");
        body.put("description", "邮件内容");
        properties.put("body", body);
        
        params.put("properties", properties);
        params.put("required", java.util.Arrays.asList("to", "subject", "body"));
        
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String to = (String) parameters.get("to");
        String subject = (String) parameters.get("subject");
        String body = (String) parameters.get("body");
        
        Map<String, Object> result = new HashMap<>();
        
        // 模拟发送邮件
        result.put("to", to);
        result.put("subject", subject);
        result.put("body", body);
        result.put("status", "sent");
        result.put("message", "邮件已成功发送（模拟）");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
