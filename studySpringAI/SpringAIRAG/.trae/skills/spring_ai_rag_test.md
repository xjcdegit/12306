# Spring AI RAG API 测试 Skill

## 描述
这是一个用于测试 Spring AI RAG 项目所有 API 端点的技能。支持智能聊天、MCP工具调用、记忆管理等功能。

## 触发关键词
- 测试API
- 测试接口
- 调用API
- Spring AI
- RAG测试
- MCP测试
- 聊天测试
- 记忆测试

## 服务信息
- 基础URL: http://localhost:8081
- 前端页面: http://localhost:8081/index.html

## API 端点列表

### 1. 智能聊天 (Skill路由)
```
GET /memory/chat?conversationId={}&userId={}&message={}
```
- 功能: 带Skill路由的智能聊天
- 参数: conversationId(会话ID), userId(用户ID), message(消息内容)
- 示例: 测试聊天，消息是"你好"

### 2. Skill匹配
```
GET /memory/skill-match?message={}
```
- 功能: 匹配用户消息到对应的Skill
- 参数: message(消息内容)
- 示例: 匹配Skill，消息是"我想去天津旅游"

### 3. MCP工具列表
```
GET /api/mcp/tools
```
- 功能: 获取所有可用的MCP工具
- 示例: 列出所有MCP工具

### 4. MCP状态
```
GET /api/mcp/status
```
- 功能: 获取MCP服务状态
- 示例: 检查MCP状态

### 5. MCP聊天
```
GET /api/mcp/chat?prompt={}
```
- 功能: 直接使用MCP工具聊天
- 参数: prompt(提示词)
- 示例: MCP聊天，查询北京天气

### 6. 记忆统计
```
GET /memory/stats?conversationId={}&userId={}
```
- 功能: 获取会话记忆统计
- 参数: conversationId, userId
- 示例: 查看记忆统计

### 7. 最近对话
```
GET /memory/recent?conversationId={}&lastN={}
```
- 功能: 获取最近N条对话
- 参数: conversationId, lastN(数量)
- 示例: 获取最近10条对话

### 8. 完整对话历史
```
GET /memory/complete?conversationId={}
```
- 功能: 获取完整对话历史
- 参数: conversationId
- 示例: 获取完整对话历史

### 9. 用户历史
```
GET /memory/user-history?userId={}&limit={}
```
- 功能: 获取用户所有对话历史
- 参数: userId, limit(限制数量)
- 示例: 查看用户历史记录

### 10. 删除会话
```
DELETE /memory/conversation/{conversationId}
```
- 功能: 删除指定会话的所有记忆
- 参数: conversationId(路径参数)
- 示例: 删除会话test-001

### 11. 简单聊天
```
GET /memory/chat-simple?conversationId={}&userId={}&message={}
```
- 功能: 不带Skill路由的简单聊天
- 参数: conversationId, userId, message
- 示例: 简单聊天测试

## 工作流程

当用户请求测试API时，按以下步骤执行：

1. **确认服务状态**
   - 检查 http://localhost:8081/api/mcp/status
   - 确认服务运行正常

2. **理解用户意图**
   - 分析用户想测试哪个API
   - 提取必要的参数

3. **构造请求**
   - 根据API端点构造完整的URL
   - 对参数进行URL编码

4. **执行请求**
   - 使用 curl 或 Invoke-WebRequest 发送请求
   - 等待响应

5. **展示结果**
   - 格式化JSON响应
   - 解释关键信息

## 使用示例

### 示例1: 测试聊天功能
用户: "测试聊天功能"
执行:
```
curl "http://localhost:8081/memory/chat?conversationId=test-001&userId=user-001&message=你好"
```

### 示例2: 查询MCP工具
用户: "列出所有MCP工具"
执行:
```
curl "http://localhost:8081/api/mcp/tools"
```

### 示例3: 测试Skill匹配
用户: "测试Skill匹配，消息是我想去北京旅游"
执行:
```
curl "http://localhost:8081/memory/skill-match?message=我想去北京旅游"
```

### 示例4: 查看记忆统计
用户: "查看会话test-001的记忆统计"
执行:
```
curl "http://localhost:8081/memory/stats?conversationId=test-001&userId=user-001"
```

## 注意事项

1. **服务必须运行**: 确保Spring Boot应用在8081端口运行
2. **URL编码**: 中文参数需要URL编码
3. **会话ID**: 使用一致的会话ID来保持对话上下文
4. **MCP工具**: 高德地图工具需要有效的API Key

## 快速启动命令

如果服务未运行，执行：
```bash
cd d:\code\project\studySpringAI\studySpringAI\SpringAIRAG
$env:JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
mvn spring-boot:run -gs .mvn/settings.xml
```

## 前端测试页面

打开浏览器访问: http://localhost:8081
可使用可视化界面测试所有API。
