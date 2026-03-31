package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 翻译工具
 * 提供文本翻译功能（模拟）
 */
@Component
public class TranslationTool implements Tool {

    @Override
    public String getName() {
        return "translation";
    }

    @Override
    public String getDescription() {
        return "将文本从一种语言翻译成另一种语言";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> text = new HashMap<>();
        text.put("type", "string");
        text.put("description", "要翻译的文本");
        properties.put("text", text);
        
        Map<String, Object> from = new HashMap<>();
        from.put("type", "string");
        from.put("description", "源语言，例如：zh（中文）、en（英文）");
        properties.put("from", from);
        
        Map<String, Object> to = new HashMap<>();
        to.put("type", "string");
        to.put("description", "目标语言，例如：zh（中文）、en（英文）");
        properties.put("to", to);
        
        params.put("properties", properties);
        params.put("required", java.util.Arrays.asList("text", "from", "to"));
        
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String text = (String) parameters.get("text");
        String from = (String) parameters.get("from");
        String to = (String) parameters.get("to");
        
        Map<String, Object> result = new HashMap<>();
        
        // 模拟翻译
        String translatedText = "【翻译结果】" + text + "（从" + from + "翻译到" + to + "）";
        
        result.put("originalText", text);
        result.put("translatedText", translatedText);
        result.put("from", from);
        result.put("to", to);
        result.put("status", "success");
        result.put("message", "翻译完成（模拟结果）");
        
        return result;
    }
}
