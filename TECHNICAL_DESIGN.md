# YHarness - 销售Agent框架技术方案

## 1. 项目概述

YHarness 是一个基于 SpringBoot 的 AI Agent 框架，专注于销售场景，支持 ReAct 循环、Context 管理和多 AI Provider 集成。

### 1.1 核心目标

- 实现完整的 ReAct 循环（Reason + Action）
- 支持多 AI Provider（OpenAI、Anthropic）
- 灵活的 Context 管理机制
- Hooks 扩展机制（参考 ClaudeCode 14个hooks）
- 配置化管理模型参数

### 1.2 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 1.8 |
| 框架 | SpringBoot | 2.7.x |
| HTTP客户端 | OkHttp | 4.12.x |
| JSON处理 | Jackson | 2.15.x |
| 日志 | SLF4J + Logback | - |

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        YHarness Agent                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌───────────────────┐   │
│  │   Client    │    │   Hooks     │    │   Configuration   │   │
│  └──────┬──────┘    └──────┬──────┘    └─────────┬─────────┘   │
│         │                  │                      │             │
│         ▼                  ▼                      ▼             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    ReAct Engine                          │   │
│  │  ┌─────────┐  ┌──────────────┐  ┌───────────────────┐   │   │
│  │  │ Reason  │→│ ActionParser │→│ ToolExecutor      │   │   │
│  │  └────┬────┘  └──────┬───────┘  └─────────┬─────────┘   │   │
│  │       │              │                     │              │   │
│  │       └──────────────┴─────────────────────┘              │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                   │
│                            ▼                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Context Manager                        │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐   │   │
│  │  │SystemPrompt│  Messages │  ToolDefs │  LongMemory │   │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └───────────┘   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                   │
│                            ▼                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Provider Layer                          │   │
│  │  ┌───────────────┐    ┌─────────────────┐                │   │
│  │  │ OpenAIProvider│    │ AnthropicProvider│               │   │
│  │  └───────────────┘    └─────────────────┘                │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

| 模块 | 职责 | 关键类 |
|------|------|--------|
| `core` | ReAct 循环引擎 | `ReActEngine`, `Thought`, `Action` |
| `context` | 上下文管理 | `AgentContext`, `Message`, `ToolDefinition` |
| `provider` | AI 模型抽象与实现 | `AIProvider`, `OpenAIProvider`, `AnthropicProvider` |
| `hooks` | 生命周期钩子 | `AgentHook`, `HookType` |
| `config` | 配置管理 | `AgentProperties`, `ProviderConfig` |
| `tools` | 工具定义与执行 | `Tool`, `ToolExecutor` |

---

## 3. 核心数据结构

### 3.1 AgentContext（上下文）

```java
public class AgentContext {
    // 系统提示词
    private String systemPrompt;
    
    // 消息列表 (user/assistant/tool)
    private List<Message> messages;
    
    // 工具定义列表
    private List<ToolDefinition> toolDefinitions;
    
    // 长期记忆
    private LongTermMemory longTermMemory;
    
    // 工具执行结果
    private String toolExecutionResult;
    
    // 对话元数据
    private Map<String, Object> metadata;
    
    // 业务状态存储（销售场景专用）
    private BusinessState businessState;
    
    // 对话ID
    private String conversationId;
}
```

### 3.1.1 BusinessState（业务状态）

销售Agent需要维护丰富的业务状态，以支持复杂的销售流程：

| 字段 | 类型 | 说明 |
|------|------|------|
| `currentStage` | `SalesStage` | 当前销售阶段：初步接触/需求分析/方案演示/商务谈判/成交 |
| `customerProfile` | `CustomerProfile` | 客户画像信息 |
| `productInterest` | `List<String>` | 客户感兴趣的产品列表 |
| `dealValue` | `BigDecimal` | 预估成交金额 |
| `nextAction` | `String` | 下一步行动计划 |
| `contactHistory` | `List<ContactRecord>` | 接触历史记录 |
| `objections` | `List<Objection>` | 客户异议记录 |
| `competitors` | `List<String>` | 竞品信息 |

### 3.1.2 CustomerProfile（客户画像）

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 客户姓名 |
| `company` | `String` | 公司名称 |
| `title` | `String` | 职位 |
| `email` | `String` | 邮箱 |
| `phone` | `String` | 电话 |
| `industry` | `String` | 所属行业 |
| `budgetRange` | `String` | 预算范围 |
| `decisionMaker` | `boolean` | 是否为决策人 |
| `painPoints` | `List<String>` | 痛点需求 |

### 3.1.3 SalesStage（销售阶段枚举）

```java
public enum SalesStage {
    INITIAL_CONTACT("初步接触"),
    NEEDS_ANALYSIS("需求分析"),
    SOLUTION_DEMO("方案演示"),
    COMMERCIAL_NEGOTIATION("商务谈判"),
    CLOSING("成交"),
    FOLLOW_UP("跟进");
}
```

### 3.2 Message（消息）

| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | `MessageRole` | 角色：user/system/assistant/tool |
| `content` | `String` | 消息内容 |
| `timestamp` | `LocalDateTime` | 时间戳 |
| `name` | `String` | 工具调用时的工具名称 |

### 3.3 ToolDefinition（工具定义）

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 工具名称 |
| `description` | `String` | 工具描述 |
| `parameters` | `List<ToolParameter>` | 参数列表 |
| `returnType` | `String` | 返回类型 |

### 3.4 Thought（思考）

```java
public class Thought {
    private String content;      // 思考内容
    private Action action;       // 下一步动作
    private boolean isFinal;     // 是否为最终回答
}
```

### 3.5 Action（动作）

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | `ActionType` | 动作类型：tool_call / finish |
| `toolName` | `String` | 工具名称 |
| `arguments` | `Map<String, Object>` | 参数 |
| `finishReason` | `String` | 结束原因 |

---

## 4. ReAct 循环流程

```
┌──────────────────────────────────────────────────────────────────────┐
│                        ReAct 循环流程                                │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────┐   │
│  │ 1. 用户输入   │───▶│ 2. 构建Prompt │───▶│ 3. 调用AI Provider │   │
│  └──────────────┘    └──────────────┘    └──────────┬───────────┘   │
│                                                      │               │
│                                                      ▼               │
│                                            ┌────────────────┐       │
│                                            │  4. 解析响应   │       │
│                                            └───────┬────────┘       │
│                                                    │                 │
│                          ┌────────────────────────┼────────────────┐ │
│                          ▼                        ▼                │ │
│                ┌─────────────────┐      ┌───────────────────┐       │ │
│                │ 5a. 工具调用    │      │ 5b. 直接回答       │       │ │
│                └────────┬────────┘      └─────────┬─────────┘       │ │
│                         │                         │                 │ │
│                         ▼                         ▼                 │ │
│                ┌─────────────────┐      ┌───────────────────┐       │ │
│                │ 6. 执行工具     │      │ 6. 返回最终答案    │       │ │
│                └────────┬────────┘      └───────────────────┘       │ │
│                         │                                           │ │
│                         ▼                                           │ │
│                ┌─────────────────┐                                  │ │
│                │ 7. 记录结果到   │──────────────────────────────────┘ │
│                │    Context      │                                  │ │
│                └─────────────────┘                                  │ │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 5. Provider 设计

### 5.1 AIProvider 接口

| 方法 | 功能 |
|------|------|
| `generate(AgentContext context)` | 调用AI生成响应 |
| `supports(ProviderType type)` | 判断是否支持该类型 |
| `getName()` | 获取Provider名称 |

### 5.2 支持的Provider

| Provider | 类型标识 | API格式 |
|----------|----------|---------|
| OpenAIProvider | `OPENAI` | OpenAI Chat Completions |
| AnthropicProvider | `ANTHROPIC` | Anthropic Messages API |

### 5.3 格式转换

**OpenAI 格式输出：**
```json
{
  "role": "assistant",
  "content": "...",
  "tool_calls": [{
    "id": "call_xxx",
    "type": "function",
    "function": {
      "name": "tool_name",
      "arguments": {...}
    }
  }]
}
```

**Anthropic 格式输出：**
```json
{
  "content": [{
    "type": "text",
    "text": "..."
  }, {
    "type": "tool_use",
    "id": "toolu_xxx",
    "name": "tool_name",
    "input": {...}
  }]
}
```

---

## 6. Hooks 机制

参考 ClaudeCode 的 14 个 hooks 设计：

| 序号 | Hook名称 | 触发时机 | 用途 |
|------|----------|----------|------|
| 1 | `onAgentStart` | Agent启动时 | 初始化资源 |
| 2 | `onAgentEnd` | Agent结束时 | 清理资源 |
| 3 | `onContextInit` | 上下文初始化 | 设置初始状态 |
| 4 | `onContextUpdate` | 上下文更新 | 监听变更 |
| 5 | `onMessageReceived` | 收到用户消息 | 预处理消息 |
| 6 | `onMessageSend` | 发送消息前 | 消息过滤/增强 |
| 7 | `onToolCall` | 工具调用前 | 参数校验/日志 |
| 8 | `onToolResult` | 工具返回后 | 结果处理 |
| 9 | `onProviderCall` | 调用AI前 | 请求修改 |
| 10 | `onProviderResponse` | AI响应后 | 响应处理 |
| 11 | `onError` | 发生错误时 | 错误处理/重试 |
| 12 | `onReActStart` | ReAct循环开始 | 记录开始时间 |
| 13 | `onReActEnd` | ReAct循环结束 | 记录耗时 |
| 14 | `onThoughtGenerated` | 生成思考后 | 思考分析 |

---

## 7. 配置设计

### 7.1 application.yml 结构

```yaml
yharness:
  provider:
    type: OPENAI  # OPENAI / ANTHROPIC
    api-key: ${AI_API_KEY}
    base-url: https://api.openai.com/v1
    model: gpt-4o-mini
    timeout: 30000
    max-tokens: 4096
    temperature: 0.7
  
  context:
    system-prompt: "你是一位专业的销售顾问..."
    max-messages: 50
  
  hooks:
    enabled: true
    packages:
      - com.example.yharness.hooks
  
  react:
    max-iterations: 10
    enable-thinking: true
```

### 7.2 配置类结构

```java
@ConfigurationProperties(prefix = "yharness")
public class AgentProperties {
    private ProviderConfig provider;
    private ContextConfig context;
    private HooksConfig hooks;
    private ReActConfig react;
}
```

---

## 8. 目录结构

```
src/main/java/com/example/yharness/
├── YHarnessApplication.java      # 启动类
├── core/                         # ReAct核心
│   ├── ReActEngine.java          # ReAct引擎
│   ├── Thought.java              # 思考对象
│   ├── Action.java               # 动作对象
│   └── ActionType.java           # 动作类型枚举
├── context/                      # 上下文管理
│   ├── AgentContext.java         # 上下文主类
│   ├── Message.java              # 消息类
│   ├── MessageRole.java          # 消息角色枚举
│   ├── ToolDefinition.java       # 工具定义
│   ├── ToolParameter.java        # 工具参数
│   ├── LongTermMemory.java       # 长期记忆
│   └── ContextManager.java       # 上下文管理器
├── provider/                     # Provider层
│   ├── AIProvider.java           # Provider接口
│   ├── ProviderType.java         # Provider类型枚举
│   ├── OpenAIProvider.java       # OpenAI实现
│   ├── AnthropicProvider.java    # Anthropic实现
│   └── ProviderFactory.java      # Provider工厂
├── hooks/                        # Hooks机制
│   ├── AgentHook.java            # Hook接口
│   ├── HookType.java             # Hook类型枚举
│   ├── HookManager.java          # Hook管理器
│   └── impl/                     # 默认Hook实现
│       ├── LoggingHook.java
│       └── MetricsHook.java
├── tools/                        # 工具模块
│   ├── Tool.java                 # 工具接口
│   ├── ToolExecutor.java         # 工具执行器
│   └── sales/                    # 销售相关工具
│       ├── ProductQueryTool.java
│       └── SalesScriptGenerator.java
├── config/                       # 配置类
│   ├── AgentProperties.java      # 配置属性
│   ├── ProviderConfig.java
│   ├── ContextConfig.java
│   ├── HooksConfig.java
│   └── ReActConfig.java
├── exception/                    # 异常处理
│   ├── AgentException.java
│   └── ProviderException.java
└── util/                         # 工具类
    ├── JsonUtils.java
    └── HttpUtils.java

src/main/resources/
└── application.yml               # 应用配置
```

---

## 9. 关键类设计

### 9.1 ReActEngine

| 方法 | 功能 |
|------|------|
| `start(AgentContext context)` | 启动ReAct循环 |
| `process(AgentContext context)` | 执行单次迭代 |
| `parseAction(String response)` | 解析AI响应中的动作 |
| `executeTool(Action action)` | 执行工具调用 |
| `buildPrompt(AgentContext context)` | 构建完整Prompt |

### 9.2 ContextManager

| 方法 | 功能 |
|------|------|
| `createContext()` | 创建新上下文 |
| `addMessage(Message message)` | 添加消息 |
| `updateToolResult(String result)` | 更新工具执行结果 |
| `getPromptForProvider(ProviderType type)` | 根据Provider类型构建Prompt |
| `clear()` | 清空上下文 |

### 9.3 ToolExecutor

| 方法 | 功能 |
|------|------|
| `execute(Action action)` | 执行动作 |
| `registerTool(Tool tool)` | 注册工具 |
| `getTool(String name)` | 获取工具 |

---

## 10. 销售场景工具

针对销售Agent场景，设计以下核心工具：

| 工具名 | 功能 | 参数 |
|--------|------|------|
| `queryProductInfo` | 查询产品信息 | `productId` |
| `generateSalesScript` | 生成销售话术 | `productId`, `customerProfile` |
| `analyzeCustomer` | 分析客户画像 | `customerId` |
| `getSalesTips` | 获取销售建议 | `scenario`, `productId` |
| `searchCompetitors` | 查询竞品信息 | `productCategory` |

---

## 11. 安全与监控

### 11.1 安全措施

- API Key 通过环境变量注入
- 敏感配置不打印日志
- 请求参数校验

### 11.2 监控指标

- ReAct循环次数
- 工具调用次数
- API响应时间
- 错误率统计

---

## 12. 后续迭代计划

| 阶段 | 目标 | 时间 |
|------|------|------|
| Phase 1 | 基础框架搭建，ReAct循环 | 第1周 |
| Phase 2 | 多Provider支持 | 第2周 |
| Phase 3 | Hooks机制完善 | 第3周 |
| Phase 4 | 销售工具开发 | 第4周 |
| Phase 5 | RAG长期记忆集成 | 第5-6周 |
