# Spring AI QuickStart 项目文档

## 项目概述

基于 Spring AI Alibaba 构建的智能对话系统，集成大语言模型(LLM)、Function Calling(函数调用)和 RAG(检索增强生成)能力，提供企业级 AI 应用开发框架。

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.12 | 基础框架 |
| Spring AI Alibaba | 1.0.0.3 | AI 能力集成 |
| MySQL | 8.x | 向量存储/知识库 |
| OpenAI API | - | 兼容接口 |

### 支持的模型

- **阿里云通义千问** (DashScope): qwen-plus, qwen-max
- **智谱 AI** (GLM): 通过 OpenAI 兼容接口

---

## 项目结构

```
src/main/java/com/springaiquickstart/
├── config/                    # 配置类
│   ├── ToolConfig.java       # 工具回调配置
│   └── ZhipuConfig.java      # 智谱AI配置
├── controller/               # 控制器
│   ├── ChatController.java   # 聊天接口
│   └── RagController.java    # RAG知识库管理
├── mcp/                      # 工具协议层(MCP)
│   ├── Tool.java             # 工具接口
│   ├── ToolRegistry.java     # 工具注册表接口
│   ├── ToolRegistryImpl.java # 工具注册表实现
│   ├── ToolDispatcher.java   # 工具调度器接口
│   ├── ToolDispatcherImpl.java # 工具调度器实现
│   ├── ToolCall.java         # 工具调用实体
│   └── ToolResponse.java     # 工具响应实体
├── rag/                      # RAG检索增强生成
│   ├── RagService.java       # RAG服务
│   ├── MySqlVectorStore.java # MySQL向量存储
│   └── DocumentLoaderService.java # 文档加载服务
├── tools/                    # 工具实现
│   ├── ToolManager.java      # 工具管理器
│   ├── CalculatorTool.java   # 计算器工具
│   ├── WeatherTool.java      # 天气查询工具
│   ├── TimeTool.java         # 时间查询工具
│   ├── TranslationTool.java  # 翻译工具
│   ├── SearchTool.java       # 搜索工具
│   ├── EmailTool.java        # 邮件工具
│   └── SqlTool.java          # SQL查询工具
└── SpringAiQuickStartApplication.java # 启动类
```

---

## 核心功能

### 1. 智能对话 (Function Calling)

系统使用 Spring AI 的 Function Calling 机制，让 LLM 自动判断是否需要调用工具。

#### 工作流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  用户消息    │ ──▶ │    LLM      │ ──▶ │  判断意图    │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                    ┌──────────────────────────┴──────┐
                    │                                 │
                    ▼                                 ▼
           ┌─────────────┐                   ┌─────────────┐
           │  需要调用工具 │                   │  直接回复    │
           └──────┬──────┘                   └─────────────┘
                  │
                  ▼
           ┌─────────────┐
           │  执行工具    │
           └──────┬──────┘
                  │
                  ▼
           ┌─────────────┐
           │  返回结果    │
           └─────────────┘
```

#### 接口说明

**聊天接口**

```
GET /ai/chat?message={用户消息}
```

**示例**

```bash
# 普通对话
curl "http://localhost:8080/ai/chat?message=你好"

# 天气查询（自动调用工具）
curl "http://localhost:8080/ai/chat?message=北京今天天气怎么样"

# 时间查询
curl "http://localhost:8080/ai/chat?message=现在几点了"

# 计算器
curl "http://localhost:8080/ai/chat?message=计算123乘以456"
```

### 2. 内置工具

| 工具名称 | 功能 | 参数 |
|---------|------|------|
| `calculator` | 数学计算 | expression: 数学表达式 |
| `weather_query` | 天气查询 | city: 城市, date: 日期 |
| `time_query` | 时间查询 | 无参数 |
| `translation` | 文本翻译 | text: 文本, target_lang: 目标语言 |
| `search` | 搜索查询 | query: 搜索关键词 |
| `email` | 发送邮件 | to: 收件人, subject: 主题, body: 内容 |
| `sql_query` | SQL查询 | sql: SQL语句 |

### 3. RAG 知识库

#### 接口说明

**添加文档**

```
POST /rag/add?title={标题}&content={内容}&category={分类}
```

**批量添加**

```
POST /rag/add-batch
Content-Type: application/json

[
  {"title": "标题1", "content": "内容1", "category": "分类1"},
  {"title": "标题2", "content": "内容2", "category": "分类2"}
]
```

**查询知识库**

```
GET /rag/query?question={问题}&topK={返回数量}
```

**删除文档**

```
DELETE /rag/document/{docId}
DELETE /rag/category/{category}
DELETE /rag/clear
```

**统计信息**

```
GET /rag/stats
```

---

## 配置说明

### application.yml

```yaml
spring:
  ai:
    dashscope:
      api-key: ${API-KEY}          # 阿里云API密钥
      chat:
        options:
          model: qwen-plus          # 模型名称
  datasource:
    url: jdbc:mysql://localhost:3306/spring_ai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

zhipu:
  ai:
    api-key: ${ZHIPU_API_KEY}      # 智谱AI密钥

weather:
  api:
    key: ${WEATHER_API_KEY}        # OpenWeatherMap密钥

logging:
  level:
    org.springframework.ai: debug
```

### 环境变量

| 变量名 | 说明 | 获取方式 |
|--------|------|----------|
| `API-KEY` | 阿里云百炼API密钥 | https://bailian.console.aliyun.com |
| `ZHIPU_API_KEY` | 智谱AI密钥 | https://open.bigmodel.cn |
| `WEATHER_API_KEY` | OpenWeatherMap密钥 | https://openweathermap.org/api |

---

## 核心代码解析

### 1. 工具接口 (Tool.java)

```java
public interface Tool {
    String getName();                              // 工具名称
    String getDescription();                       // 工具描述
    Map<String, Object> getParameters();           // 参数定义(JSON Schema)
    Map<String, Object> execute(Map<String, Object> parameters); // 执行方法
}
```

### 2. 工具注册配置 (ToolConfig.java)

将自定义 Tool 转换为 Spring AI 的 ToolCallback：

```java
@Bean
public List<ToolCallback> toolCallbacks(ToolRegistry toolRegistry) {
    List<ToolCallback> callbacks = new ArrayList<>();
    
    for (Tool tool : toolRegistry.getAvailableTools()) {
        String inputSchema = objectMapper.writeValueAsString(tool.getParameters());
        
        ToolCallback callback = FunctionToolCallback.builder(tool.getName(), 
                (Map<String, Object> params) -> tool.execute(params))
                .description(tool.getDescription())
                .inputType(Map.class)
                .inputSchema(inputSchema)
                .build();
        callbacks.add(callback);
    }
    
    return callbacks;
}
```

### 3. 聊天控制器 (ChatController.java)

```java
@GetMapping("/chat")
public String chat(@RequestParam String message) {
    return chatClientBuilder.build()
            .prompt(message)
            .toolCallbacks(toolCallbacks)  // 注入工具
            .call()
            .content();
}
```

### 4. 天气工具示例 (WeatherTool.java)

```java
@Component
public class WeatherTool implements Tool {
    
    @Override
    public String getName() {
        return "weather_query";
    }
    
    @Override
    public String getDescription() {
        return "查询指定城市的天气信息";
    }
    
    @Override
    public Map<String, Object> getParameters() {
        // JSON Schema 格式的参数定义
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "city", Map.of("type", "string", "description", "城市名称"),
                "date", Map.of("type", "string", "description", "查询日期")
            ),
            "required", List.of("city")
        );
    }
    
    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String city = (String) parameters.get("city");
        String date = (String) parameters.getOrDefault("date", "今天");
        // 调用天气API...
        return result;
    }
}
```

---

## 扩展开发

### 添加新工具

1. 实现 `Tool` 接口：

```java
@Component
public class MyNewTool implements Tool {
    
    @Override
    public String getName() {
        return "my_tool";
    }
    
    @Override
    public String getDescription() {
        return "工具描述";
    }
    
    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "param1", Map.of("type", "string", "description", "参数说明")
            ),
            "required", List.of("param1")
        );
    }
    
    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        // 实现工具逻辑
        return Map.of("result", "执行结果");
    }
}
```

2. 工具会自动被 `ToolRegistry` 注册，无需额外配置。

---

## 数据库设计

### 向量存储表 (vector_store)

```sql
CREATE TABLE vector_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_id VARCHAR(255) NOT NULL,
    title VARCHAR(500),
    content TEXT NOT NULL,
    embedding BLOB,
    category VARCHAR(100),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doc_id (doc_id),
    INDEX idx_category (category)
);
```

---

## 部署说明

### 本地运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行JAR
java -jar target/SpringAIQuickStart-0.0.1-SNAPSHOT.jar
```

### Docker 部署

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/SpringAIQuickStart-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t spring-ai-app .
docker run -p 8080:8080 -e API-KEY=your_key spring-ai-app
```

---

## 常见问题

### Q: 工具调用失败？

检查以下几点：
1. 工具是否正确实现 `Tool` 接口
2. 参数定义是否符合 JSON Schema 格式
3. 查看日志中的工具调用详情

### Q: 天气查询返回错误？

1. 检查 OpenWeatherMap API Key 是否有效
2. 城市名是否支持（已内置50+中国城市中英文映射）
3. 检查网络连接

### Q: RAG 查询无结果？

1. 确认已添加文档到知识库
2. 检查 MySQL 连接是否正常
3. 确认向量维度配置正确

---

## 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 0.0.1-SNAPSHOT | 2026-04 | 初始版本，支持Function Calling和RAG |

---

## 许可证

MIT License
