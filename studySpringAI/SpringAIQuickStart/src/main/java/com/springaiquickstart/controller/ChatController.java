package com.springaiquickstart.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 聊天控制器
 * 
 * 提供AI聊天功能和智能工具调用接口
 * 使用Spring AI的Function Calling功能，让LLM自动判断是否调用工具
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient.Builder chatClientBuilder;
    private final List<ToolCallback> toolCallbacks;

    @Autowired
    public ChatController(ChatClient.Builder chatClientBuilder, 
                          List<ToolCallback> toolCallbacks) {
        this.chatClientBuilder = chatClientBuilder;
        this.toolCallbacks = toolCallbacks;
    }

    /**
     * 智能聊天接口
     * 
     * 工作流程：
     * 1. 用户发送消息
     * 2. 将消息和工具定义发送给LLM
     * 3. LLM判断是否需要调用工具，以及调用哪些工具和参数
     * 4. 自动执行工具调用并返回结果
     * 5. 返回最终处理结果
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        System.out.println("========================================");
        System.out.println("收到用户消息: " + message);
        System.out.println("可用工具数量: " + toolCallbacks.size());
        
        String response = chatClientBuilder.build()
                .prompt(message)
                .toolCallbacks(toolCallbacks)
                .call()
                .content();
        
        System.out.println("AI回复: " + response);
        System.out.println("========================================");
        return response;
    }
}
