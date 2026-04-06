package com.springaiquickstart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springaiquickstart.mcp.Tool;
import com.springaiquickstart.mcp.ToolRegistry;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class ToolConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public List<ToolCallback> toolCallbacks(ToolRegistry toolRegistry) {
        List<ToolCallback> callbacks = new ArrayList<>();
        
        for (Tool tool : toolRegistry.getAvailableTools()) {
            try {
                String inputSchema = objectMapper.writeValueAsString(tool.getParameters());
                
                ToolCallback callback = FunctionToolCallback.builder(tool.getName(), (Map<String, Object> params) -> tool.execute(params))
                        .description(tool.getDescription())
                        .inputType(Map.class)
                        .inputSchema(inputSchema)
                        .build();
                callbacks.add(callback);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create tool callback for: " + tool.getName(), e);
            }
        }
        
        return callbacks;
    }
}
