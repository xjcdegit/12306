# SpringAISkills 设计文档

## 1. 核心概念

| 概念 | 层次 | 职责 |
|------|------|------|
| **MCP** | 协议层 | 标准化的工具调用协议，负责传输 |
| **原子 Tool** | 协议层 | 最小操作单元，如查询数据库、调用 API |
| **Skill** | 业务层 | 工作流，封装多个原子 Tool 完成业务目标 |
| **Agent** | 调度层 | 管理 LLM 与 Skill 之间的交互循环 |

---

## 2. 系统工作流

### 阶段 1：Agent 接收与会话初始化

接收输入：用户通过前端界面发送自然语言指令。


### 阶段 2：获取 MCP 工具集

Agent 需要知道当前系统具备哪些能力。

并行广播：Agent 作为 MCP Client，向所有 MCP Server 发送 tools/list 的 JSON-RPC 请求。

聚合工具池：各个 MCP Server 返回各自的工具元数据，Agent 将这些数据聚合在内存池中。

### 阶段 3：构建大模型载荷

Agent 组装发送给 LLM 的最终请求包。

- 注入 System Prompt：设定 Agent 的核心人设与硬性约束
- 拼接 Message 数组：将历史对话记录与当前用户输入组装
- 挂载 Tools Schema：将 MCP 工具转换为 tools 数组附加在请求体中

### 阶段 4：LLM 决策推理

大模型进行意图识别和规划。

- 意图匹配：LLM 分析用户需求
- 工具匹配：LLM 扫描 tools 数组，匹配所需工具
- 生成挂起指令：LLM 返回 tool_calls 响应，输出结构化调用指令

### 阶段 5：MCP 路由与物理执行

真正的业务代码执行阶段。

- 拦截与路由：Agent 解析工具名称，查找归属的 MCP Server
- 触发 MCP 调用：Agent 通过 MCP 协议发送 tools/call 请求
- 物理执行：MCP Server 执行底层代码
- 结果返回：Server 将结果打包成 JSON 传回 Agent

### 阶段 6：闭环回传与最终生成

Agent 将执行结果回传给 LLM。

- 追加工具结果：将 JSON 结果封装成 role: tool 消息，追加到对话上下文
- 二次请求 LLM：Agent 再次将 messages 数组发送给 LLM
- 连续规划：LLM 判断任务是否完成，可能再次触发 tool_calls（循环）
- 生成最终文本：LLM 结合所有结果生成自然语言回复
- 响应用户：Agent 将最终响应返回给用户

---

## 3. 流程图

```text
用户输入
    ↓
阶段1: Agent 接收 → 加载会话历史
    ↓
阶段2: MCP tools/list → 聚合工具池
    ↓
阶段3: 构建 Prompt + Tools Schema
    ↓
阶段4: LLM 决策 → tool_calls?
    ↓
阶段5: MCP tools/call → 执行 → 返回结果
    ↓
阶段6: 结果回传 LLM → 循环或生成响应
    ↓
返回用户
```

---

## 4. Skill（工作流）内部结构

```text
Skill
├── 原子 Tool A
├── 原子 Tool B
├── 原子 Tool C
└── ...
```

Skill 内部封装了完整的业务流程，按预定顺序调用多个原子 Tool。

---

## 5. 模块结构

```text
src/main/java/com/springai/skills
├── config/           # 自动配置
├── agent/            # Agent 调度器
├── core/             # 核心接口（Tool、Skill、ToolResult）
├── registry/         # Tool/Skill 注册中心
├── mcp/              # MCP 协议实现
├── tools/            # Tool 相关
│   ├── atomic/       # 原子 Tool 实现
│   └── skills/       # Skill 实现（工作流）
└── controller/       # API 接口
```

---

## 6. 职责边界

- **MCP**：协议传输，不包含业务逻辑
- **原子 Tool**：最小操作单元，输入输出结构化
- **Skill**：工作流封装，编排多个原子 Tool
- **Agent**：调度闭环，管理 LLM 与 Skill 的交互循环
