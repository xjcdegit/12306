package com.springaiquickstart.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZhipuConfig {

    @Value("${zhipu.ai.api-key}")
    private String apiKey;

    // 将初始化好的 Client 注册为 Bean
    @Bean
    public ZhipuAiClient zhipuAiClient() {
        return ZhipuAiClient.builder()
                .ofZHIPU()
                .apiKey(apiKey)
                .build();
    }
}
