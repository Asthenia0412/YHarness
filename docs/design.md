# Agent 系统设计

## 设计目标

本文档描述一个**完整可实现的、面向企业生产场景的 Agent 系统**。阅读这份文档后，你可以从头搭建一个具备任务调度、推理、记忆、工具调用、治理、评测能力的生产级 Agent。

### Agent 的能力边界

这个 Agent 是一个**可信查询与辅助决策系统**。它的核心能力：

- 接收用户自然语言输入
- 支持两种入口：**Inbound（被动响应）**——商家发消息触发；**OutReach（主动触达）**——系统定时任务触发
- 自动理解意图，查询外部事实（CRM 数据、广告消耗、优惠政策、知识库等），推理出有价值的回复
- 在单次连线内保持上下文连续（知道"刚才聊到哪儿了"）
- 跨连线记住用户是谁、偏好什么、历史上发生过什么
- 通过只读 Tool 连接外部世界，但**不执行任何写入操作**
- 主动向客户发起广告推介，文案风格在首次触达时受模板约束，后续对话中自由发挥

它不是一个自动化执行系统——它不代替用户下单、退款、发消息。它只负责"查 + 想 + 说"。

### Agent 的子系统

Agent 由七个子系统构成：

| 子系统 | 解决什么问题 |
|--------|------------|
| **Task Scheduler** | 谁决定什么时候该找谁聊——任务的创建、编排、分发、串行化 |
| **ReActLoop** | Agent 怎么跑——推理-行动-观察的执行闭环 |
| **Memory（三层记忆）** | Agent 记什么——从单次执行到跨轮长期记忆的收敛体系 |
| **Context 组装** | Agent 怎么用记忆——每次推理前按优先级把信息拼装成 Context |
| **Hooks** | Agent 怎么被治理——关键生命周期的观测、注入、审计、安全 |
| **Tools（只读查询）** | Agent 怎么查——连接外部系统拿事实，但绝不写入 |
| **Eval 评测** | Agent 怎么被验证——控制变量法量化每一次改动的效果 |

### 核心设计原则

1. **Agent 是主角**：所有子系统服务于 Agent 的推理，记忆体系是 Agent 的记忆体系，不是独立的记忆产品
2. **不引入额外对象**：只基于 `Session` / `Conversation` / `Story` 三层记忆载体；`Thread / Run / AgentState` 是代码组织层面的技术容器，不是新增业务对象
3. **只读 Tool**：查询事实，不写入外部系统，安全边界天然收敛
4. **Context 组装有顺序**：先保连续性，再补身份，再补事实，最后补知识
5. **写回有节制**：`Session → Conversation` 是实时的，`Conversation → Story` 是条件的
6. **Hooks 不污染主流程**：只做观测、注入、治理、写回四类事
7. **评测用控制变量法**：每次只改一个维度，拿数据说话

---

## 系统架构总览

Agent 的整体工作流程分三段：**调度层**（谁决定什么时候该找谁）、**准备层**（将原始任务转成 Agent 理解的请求）、**执行层**（推理 + 记忆 + 工具 + 治理 → 输出 + 写回）。评测体系环绕整个 Agent，用于验证每一次变更的效果。

```
                              ┌─────────────────────────────┐
                              │       Eval 评测体系          │
                              │     （环绕整个 Agent）        │
                              └──────────────┬──────────────┘
                                             │ 验证
┌────────────────────────────────────────────┼────────────────────────────┐
│                               Agent 系统                              │
│                                             │                        │
│  ┌──────────────── 调度层 ────────────────┐  │                        │
│  │                                        │  │                        │
│  │    Inbound/OutReach → Planner →        │  │                        │
│  │    → Dispatcher → Engine.ProcessTask   │  │                        │
│  │    （Job 创建 → 锁竞争 → 分发执行）     │  │                        │
│  └───────────────────┬────────────────────┘  │                        │
│                      │                       │                        │
│                      ▼                       │                        │
│  ┌──────────────── 准备层 ────────────────┐  │                        │
│  │                                        │  │                        │
│  │    TaskPayload → RequestPreparer       │  │                        │
│  │    → AgentRequest（干净请求）           │  │                        │
│  │    （只解析运行环境，不捞业务数据）      │  │                        │
│  └───────────────────┬────────────────────┘  │                        │
│                      │                       │                        │
│                      ▼                       │                        │
│  ┌──────────────── 执行层 ────────────────┐  │                        │
│  │                    Agent.handle         │  │                        │
│  │    ┌─────────────────────────────────┐ │  │                        │
│  │    │          AgentState              │ │  │                        │
│  │    │  ├─ InputState    （只读）       │ │  │                        │
│  │    │  ├─ RuntimeState  （读写）       │ │  │                        │
│  │    │  ├─ OutputState   （只写）       │ │  │                        │
│  │    │  └─ PerfState     （读写）       │ │  │                        │
│  │    └─────────────────────────────────┘ │  │                        │
│  │                                         │  │                        │
│  │    Context: Story→Conversation→KV→Vec  │  │                        │
│  │         → ReActLoop: Thought→Action→Obs│  │                        │
│  │         → Hooks: 治理/观测/审计         │  │                        │
│  │         → Tools: 只读查询外部事实        │  │                        │
│  │         → 写回: Memory（摘要→Conv→Story)│  │                        │
│  └───────────────────┬────────────────────┘  │                        │
│                      │                       │                        │
│                      ▼                       │                        │
│                 AgentResponse                │                        │
│    finalReply + toolCalls + tokenUsage       │                        │
└──────────────────────────────────────────────┴────────────────────────┘
```

### Agent 的核心接口

在进入各子系统细节之前，先定义 Agent 自身的顶层契约：

```java
public interface Agent {

    /** 处理一次 Agent 请求，返回完整输出 */
    AgentResponse handle(AgentRequest request);

    /** 返回 Agent 当前配置 */
    AgentConfig getConfig();

    /** 返回评测目标，供评测平台调度 */
    EvalTarget getEvalTarget();
}
```

```java
public class AgentRequest {
    private String userId;             // 商家 ID
    private String conversationId;     // 会话 ID（已有或新建）
    private String userMessage;        // 商家消息（OutReach 时为空）
    private TaskType taskType;         // INBOUND / OUTREACH
    private String languageCode;       // en / th / id
    private String channelId;          // whatsapp / email
    private String channelAccountId;   // 具体渠道账号 ID（Dispatcher 分配或 Job 预绑定）
    private String accountId;          // 广告账户 ID
    private String timezone;           // Asia/Bangkok
    private Map<String, Object> metadata;
}
```

```java
public class AgentResponse {
    private String sessionId;
    private String finalReply;
    private List<ToolCallRecord> toolCalls;
    private TokenUsage tokenUsage;
    private long elapsedMs;
}
```

```java
public class AgentConfig {
    private String agentId;
    private ModelConfig modelConfig;
    private MemoryConfig memoryConfig;
    private ContextConfig contextConfig;
    private List<HookConfig> hooks;
    private ToolRegistryConfig toolRegistry;
    private ReActConfig reactConfig;
}
```

### 核心组件与职责

| 组件 | 在 Agent 中的角色 | 核心职责 |
|------|-------------------|----------|
| **Agent** | 主体 | 接收请求、调度子系统、返回响应 |
| **Task Scheduler** | 任务编排 | Planner 创建/取消 Job，Dispatcher 锁竞争后分发，Engine.ProcessTask 入口 |
| **RequestPreparer** | 请求准备 | 将原始 TaskPayload 解析为干净的 AgentRequest（只做环境解析，不做业务预取） |
| **AgentState** | 运行时容器 | InputState（只读）+ RuntimeState（读写）+ OutputState（只写）+ PerfState（读写） |
| **ReActLoop** | 执行引擎 | Thought-Action-Observation 循环，驱动推理和工具调度 |
| **Memory（三层）** | 记忆体系 | Session 持久化记录，Conversation 保持本轮连续，Story 沉淀跨轮画像 |
| **Context Assembly** | 上下文装配 | 每次推理前按优先级拼装 Conversation / Story / KV / Vector 四类上下文 |
| **Hooks** | 治理框架 | 在关键生命周期做观测、注入、审计、安全校验，不污染主流程 |
| **Tools** | 查询能力层 | 只读查询外部事实和知识，不承担写入和状态变更 |
| **Eval** | 质量保障 | 控制变量法验证每一次变更的效果 |

### 一条请求的完整旅程

```
Inbound（商家消息） / OutReach（定时任务）
    →
Planner.PlanInbound / PlanOutreach（创建 Job，串行化）
    →
Dispatcher.DispatchDue（锁竞争 → 分发到 Engine）
    →
Engine.ProcessTask → 构建 TaskPayload
    →
RequestPreparer.prepare（解析环境 → AgentRequest）
    →
Agent.handle(AgentRequest)
    → 初始化 AgentState（InputState / RuntimeState / OutputState / PerfState）
    → 定位 Memory：Story(userId) → Conversation(convId)
    → Context 组装：Conversation → Story → KV/DB → Vector
    → Hooks 前置治理：onSessionStart / onContextAssembling / onMemoryRetrieved
    → ReActLoop:
        loop: Thought → Action(Tool) → Observation → ...
    → Hooks 后置治理：onBeforeSessionSummarize
    → 提炼摘要 → 写回 Conversation → 条件更新 Story
    → 返回 AgentResponse(最终回复 + 用量 + 耗时)
```

---

## 一、核心执行引擎：ReActLoop

`ReActLoop` 是整个 Agent 的执行核心，它不负责"存记忆"，但它必须消费记忆、产生记忆、触发写回。

它的职责可以概括成四件事：

1. 消费当前 `Context`
2. 推理下一步该不该调用 `Tool`
3. 接收 `Observation` 后继续迭代
4. 在结束时产出本次 `Session` 的可沉淀结果

推荐执行流程如下：

```text
receive user input
    ->
init session
    ->
assemble context
    ->
beforeReAct hooks
    ->
loop until finish or maxIterations:
    1. call model -> thought / action
    2. if final answer:
           break
    3. dispatch tool
    4. get observation
    5. append observation to session context
    6. continue next iteration
    ->
afterReAct hooks
    ->
summarize session
    ->
write back conversation
    ->
conditionally update story
    ->
return final response
```

### ModelProvider 统一抽象

ReActLoop 的核心调用是"调模型拿 thought"。但系统可能对接豆包、GPT、Claude 等多种模型，且各自的 function calling 协议不同。因此将模型调用抽象为统一接口：

```java
public interface ModelProvider {
    ChatResponse chat(ChatRequest request);
}

// 每种模型一个适配器
public class DoubaoProvider implements ModelProvider { ... }
public class OpenAiProvider implements ModelProvider { ... }
```

ReActLoop 只依赖 `ModelProvider`，不关心底层是哪个模型。切换模型只改一行配置。

```text
ReActLoop 中的模型调用：
  → 构造 ChatRequest（含 message list + tool definitions）
  → ModelProvider.chat(request)
  → 拿到 ChatResponse（含 thought + 可能的 tool calls）
  → 判断：final 还是需要 dispatch tool
```

### ReActLoop 和三层记忆 + AgentState 的关系

- **AgentState** 是 ReActLoop 的运行容器（含 InputState / RuntimeState / OutputState / PerfState）
- `Conversation` 是 ReActLoop 的主要历史输入
- `Story` 是 ReActLoop 的长期补充输入
- `Session` 是 ReActLoop 执行结束后的**持久化记录**，不是运行时容器

也就是说：

> ReActLoop 运行时靠 AgentState 承载状态，结束时靠 Session 做持久化存档。

---

## 二、记忆体系：三层模型

记忆是 Agent 最关键的子系统之一。没有记忆，Agent 每一轮都要从零开始推理——不知道聊到哪儿了，不知道你是谁，之前确认过的事实全部丢失。

Agent 不需要一个"独立的通用记忆系统"。它只需要三件事：**知道刚才发生了什么**（Conversation）、**知道这个人是谁**（Story）、**知道这一次执行中的临时状态以及执行后如何存档**（运行时靠 AgentState 承载，结束时靠 Session 持久化）。

### 2.1 Session 的定位澄清

`Session` 是 Agent 执行结束后的一条**持久化记录**，不是运行时容器。

很多系统设计容易把 Session 当成"运行中的状态对象"，但在本系统中，运行时状态由 **AgentState** 承载（见下文「AgentState 运行时状态体系」），Session 只负责执行完成后的事：

- 记录本次执行的摘要、状态、工具调用记录
- 写回 `Conversation`（供后续 Session 读取）
- 审计和排查时查询

所以更准确的理解是：

> AgentState 是运行中，Session 是运行后。

### 2.2 Conversation

`Conversation` 是一次连线内的连续对话容器。

它关注的是"这一轮已经聊到了哪里"，所以它存的是：

- 本轮所有 `Session` 的累积摘要
- 当前对话状态
- 当前对话中的关键槽位或任务状态
- 最近一段对话里已经确认过的事实
- 当前仍然有效的临时偏好和约束

例如在广告销售场景中，`Conversation` 可以持续保留：

- 商家当前广告消耗趋势
- 已经讨论过的优惠政策
- 商家是否表达了兴趣或犹豫
- 是否已经推送过试投方案

`Conversation` 的核心价值是：

> 让同一个 Conversation 内的第 N 个 Session，知道前面 N-1 个 Session 已经发生了什么。

所以 `Conversation` 记忆必须是实时累加的，而不是等整个对话结束后才总结。

### 2.3 Story

`Story` 是用户维度的长期记忆。

它不关心某一轮具体怎么聊，而关心这个用户长期稳定的画像和历史轨迹，例如：

- 用户身份信息
- 稳定偏好
- 历史关注主题
- 历史 Conversation 的摘要索引
- 长期形成的业务判断，例如价格敏感、品牌偏好、决策周期长短

`Story` 不是当前对话的逐字记录，而是跨 Conversation 仍然有价值的信息沉淀层。

可以把它理解成：

> Conversation 解决"刚才聊到哪儿了"，Story 解决"这个人是谁、以前发生过什么"。

### 2.4 三层职责表

| 层级 | 存什么 | 怎么用 |
|------|--------|--------|
| `Session` | 单次 Input / Output、thought、action、observation、tool result | 执行结束后提炼并写回 `Conversation` |
| `Conversation` | 本轮所有 Session 的累积摘要、当前任务状态、已确认事实、临时偏好 | 保持一次连线内的上下文连续 |
| `Story` | 用户画像、长期偏好、历史 Conversation 摘要索引 | 支持跨连线的长期记忆和个性化响应 |

这三层是一个天然的"短期到长期"的收敛结构：

- `Session` 负责产生信息
- `Conversation` 负责沉淀本轮连续性
- `Story` 负责沉淀跨轮稳定性

### 2.5 写回机制：每层记自己该记的东西

这个系统最关键的不是"怎么取"，而是"执行完之后怎么写回"。

#### Session → Conversation

当一次 `Session` 执行完成后，需要把本轮过程中值得保留的内容提炼后写回 `Conversation`。

应该写回的内容包括：

- 本轮用户核心诉求
- 本轮模型给出的关键结论
- 本轮已确认的事实
- 本轮产生的任务状态变化
- 本轮工具调用得到的重要结果
- 下一轮仍然需要记住的临时上下文

不应该原样写回的内容包括：

- 冗长的思考链明文
- 低价值的中间推理噪音
- 可以重新查询且成本很低的临时结果
- 高冗余逐字对话

所以写回 `Conversation` 的不是 transcript，而是 summary + state。

#### Conversation → Story

不是每个 `Session` 都要写 `Story`，而是在满足条件时把 `Conversation` 中已经稳定的信息沉淀进 `Story`。

触发条件通常包括：

- 当前 Conversation 结束
- 当前 Conversation 达到一定长度
- 识别到新的稳定偏好
- 识别到新的用户画像信息
- 识别到高价值业务事件

应该写进 `Story` 的内容包括：

- 稳定偏好，例如品牌偏好、预算层级、风险偏好
- 长期意图，例如准备买车、准备装修、准备出国
- 关键事件摘要，例如已下单、已试驾、已投诉、已退款
- 历史 Conversation 的摘要索引

#### KV/DB 与 Vector Store 不承担主记忆写回

`KV/DB` 和 `Vector Store` 在这个设计里是外部补充源，而不是主记忆对象。

它们的定位分别是：

- `KV/DB`：提供结构化业务真相
- `Vector Store`：提供语义知识和可解释材料

除非业务明确要求，否则不建议把每次 Session 都反向写入向量库。  
否则会很快把"用户运行时噪音"污染成"知识库内容"。

结论就是：

- 会话连续性写进 `Conversation`
- 长期用户价值写进 `Story`
- 业务事实保留在 `KV/DB`
- 知识内容保留在 `Vector Store`

### 2.6 记忆的主键体系

"用什么键查记忆"直接影响 Agent 的上下文完整性和数据隔离性。

#### 核心原则

`channelAccountId + userId` 共同确定记忆归属：

- `channelAccountId` 是具体渠道账号（如 WhatsApp Business A 号），不同渠道账号对同一商家的对话是隔离的
- `userId` 是商家在 CRM 中的唯一标识

#### 主键方案（方案 C）

| 记忆层 | 主键 | 说明 |
|--------|------|------|
| `Story` | `userId`（统一画像，字段标记来源渠道） | 跨渠道统一用户画像，每个字段标识来自哪个渠道账号 |
| `Conversation` | `(channelAccountId, userId, conversationId)` | 不同渠道账号的对话完全隔离，不会互相污染 |
| `Session` | `(channelAccountId, userId, conversationId, sessionId)` | 自然继承 Conversation 的主键 |

#### Story 的渠道分来源标记

```java
public class Story {
    private String userId;                       // 主键
    private String leadStage;                    // 结构化——商机阶段
    private String language;                     // 结构化——偏好语言
    private LocalDateTime lastContactAt;
    private int totalConversations;
    private int version;
    private LocalDateTime updatedAt;

    // 结构化摘要索引
    private List<ConversationSummary> recentConversations;

    // 扩展区：Agent 自动写入的自演化键值对，按来源渠道标记
    private Map<String, String> channelProfiles;
    // key = "whatsappA", value = "{"industry":"retail","spendLevel":"medium"}"
    // key = "whatsappB", value = "{"industry":"retail","spendLevel":"low"}"

    private List<String> interestTags;           // 跨渠道聚合的兴趣标签
}
```

#### 查询路径

```
Agent 请求进入：
  channelAccountId + userId → 定位 Story(userId)
  → 构造 Conversation 主键 (channelAccountId, userId, conversationId)
  → 从 Conversation 捞当前上下文
  → 创建 Session（继承 Conversation 主键 + sessionId）
```

### 2.7 为什么不需要额外 Memory 对象

很多系统设计容易继续抽象出一层：

- `Memory`
- `MemoryItem`
- `MemoryTimeline`
- `PlannerState`

这些抽象不是不能做，而是在当前系统里没有必要。

因为现有三层已经足够表达三件事：

1. 这次执行发生了什么：`Session`
2. 这轮对话当前到了哪里：`Conversation`
3. 这个用户长期是什么样：`Story`

再额外引入对象，往往只会带来这些问题：

- 写入路径更复杂
- 检索优先级更模糊
- 生命周期边界变差
- 业务开发者更难理解

因此这个方案坚持：

> 不通过新增抽象解决问题，而通过明确分层职责解决问题。

---

## 三、AgentState 运行时状态体系

AgentState 是 ReActLoop 的核心运行时容器，只在 Agent.handle 执行期间存在，执行结束后销毁或用于生成 Session 记录。

**为什么需要 AgentState 而不是让 Session 承担这个角色？**

Session 是持久化模型，需要序列化存入数据库。AgentState 包含运行时指针、缓存、连接等无法序列化的资源，两者职责天然不同。

### 3.1 AgentState 的结构

```java
public class AgentState {
    private AgentInputState inputState;        // 只读：输入数据
    private AgentRuntimeState runtimeState;    // 读写：运行时消息与缓存
    private AgentOutputState outputState;      // 只写：最终输出
    private AgentPerformanceState perfState;   // 读写：性能统计
}
```

```
AgentState（运行时容器）
  ├── InputState（只读）
  │     ├── TaskType        （INBOUND / OUTREACH）
  │     ├── ClientProfile   （商家画像）
  │     ├── Language        （语言代码）
  │     ├── InputMessages   （输入对话历史）
  │     └── AccountId / ChannelBindingId / ThreadId
  │
  ├── RuntimeState（读写）
  │     ├── RuntimeMessages （输入 + Agent 执行产生的消息）
  │     ├── AttachmentCache （附件缓存，避免重复下载）
  │     └── Warnings        （运行时警告）
  │
  ├── OutputState（只写）
  │     ├── FinalAnswers    （最终回复消息）
  │     ├── HandoffResult   （转人工结果）
  │     ├── EvaluationResult（评测结果）
  │     └── FinishTaskResult（任务完成标记）
  │
  └── PerfState（读写）
        ├── TokenUsage      （累计 token 消耗）
        └── StartTime       （开始时间，用于计算总耗时）
```

### 3.2 AgentState 的生命周期

```text
Agent.handle 开始
  → 创建 AgentState
  → InputState.InitFromAgentInput(request)    ← 冻结输入数据
  → RuntimeState.InitFromAgentInput(messages) ← 预加载附件缓存
  → ReActLoop 每轮读写 RuntimeState
     → RuntimeMessages 不断追加
     → PerfState 累积 token
  → ReActLoop 结束
     → OutputState 成型
  → 提炼摘要 → 写回 Memory
  → 生成 Session 记录（从 AgentState 提取持久化部分）
  → AgentState 生命周期结束
```

## 四、Story 的存储结构设计

Story 采用混合结构（C 方案）：

```java
public class Story {
    private String userId;
    private String leadStage;              // 商机阶段（结构化）
    private String language;               // 偏好语言（结构化）
    private LocalDateTime lastContactAt;   // 上次接触时间（结构化）
    private int totalConversations;        // 总对话数（结构化）

    // 结构化摘要索引
    private List<ConversationSummary> recentConversations;

    // 扩展区：Agent 自动写入的自由键值对
    private Map<String, String> profileAttributes;  // "industry" → "retail"
    private List<String> interestTags;

    private int version;
    private LocalDateTime updatedAt;
}
```

核心字段有类型约束（SQL 可索引），扩展字段让 Agent 可自演化。不因"临时加个标签"就需要改表 DDL。

## 五、存储方案：旁路缓存 + Redis Checkpoint + 版本乐观锁

### 整体方案

```
Redis（旁路缓存 + checkpoint）
  ├── conv:${conversationId}:cache      → 活跃 Conversation 全量数据（TTL: 24h）
  ├── conv:${conversationId}:checkpoint → 中间 checkpoint 数据（TTL: 1h）
  └── story:${userId}:cache             → 活跃 Story 数据（TTL: 24h）

MySQL（持久层）
  ├── conversation 表（带 version 字段）
  ├── story 表（带 version 字段）
  └── session 表（只追加）
```

### 读取路径（Cache Aside）

```
读 Conversation：
  1. 查 Redis conv:${id}:cache → 命中直接返回
  2. 未命中 → 查 MySQL → 写入 Redis（设置 TTL）
  3. 返回

读 Story 同理。
```

### 写回路径

**Session → Conversation（正常结束）：**

```
ReActLoop 结束 → 提炼摘要
  → UPDATE conversation
      SET summary = ?, state_delta = ?,
          session_type = ?, version = version + 1
      WHERE conversation_id = ? AND version = :oldVersion
  → CAS 失败 → 重试或告警
  → DEL conv:${id}:cache        ← 失效旁路缓存
```

**Session 中间 Checkpoint（ReActLoop 每 3-5 轮）：**

```
Redis SET conv:${id}:checkpoint = current_state（TTL: 1h）
不碰 MySQL
```

**崩溃恢复：**

```
Session 开始时检测 Redis 有 checkpoint
  → 有：恢复 RuntimeState → 继续 ReActLoop
  → 无：从头开始
```

### 版本乐观锁（防御性设计）

即使 Conversation 内理论上串行，version 乐观锁仍作为防御性设计存在，防止任何并发写覆盖的可能。

## 六、Session 摘要格式与 sessionType

### 摘要格式（方案 C：纯文本 + 关键字段标记）

```
[INTENT] 询问 SUV 推荐和库存
[STATE] user_showed_interest
[FACTS] budget:20万左右, preference:德系SUV
[SESSION_TYPE] INBOUND

用户询问了三款 SUV（Model X/Y/Z）的库存和价格。已确认预算在 20 万左右，偏好德系。
已提供对比表格。用户表示会考虑，推荐了试驾预约，用户未确认。
```

结构化字段头 + 自然语言详情，关键信息可程序化提取，详情可自然表达。

### sessionType 标记

Conversation 的 Session 列表中的每条记录都携带标记：

```java
public enum SessionType {
    INBOUND,    // 商家主动发消息触发
    OUTREACH,   // Agent 主动触达
    TASK_TRACING  // 任务追踪
}
```

让模型知道"这个轮次是 Agent 主动发起的，商家的沉默是正常的"，避免负反馈污染历史判断。

---

## TaskScheduler 任务调度子系统

TaskScheduler 是 Agent 系统的入口层，负责将业务事件转译为调度队列中的 Job，并保证分发执行。它不属于 Agent 核心内部的 ReActLoop / Memory / Tools，而是在 Agent 之前做任务的创建、排期、串行化、分发。

### 核心组件

| 组件 | 职责 |
|------|------|
| **Planner** | 将业务事件（Inbound / OutReach）翻译为 ClientTaskJob，计算工作时间窗口，不负责"该不该触达"的决策 |
| **ClientTaskJob** | 调度队列的最小单元，带 dueAt、租约、状态、TaskKey（幂等去重用） |
| **Dispatcher** | 扫描到期 Job → 竞争租约 → 客户端级分布式锁 → 渠道账号轮询 → 调用 Executor |
| **Executor** | 执行器，调用 Engine.ProcessTask |
| **Engine** | 组装 TaskPayload → 调用 RequestPreparer → 调用 Agent.handle |

### Planner 的定位

**Planner 不是一个 AI 驱动的触达决策器，它是一个纯规则的任务翻译器。** 它不做"该不该找这个商家聊"的判断——这个决策由上游的 StrategyExecution 体系或业务事件触发。

Planner 只干三件事：

1. **翻译**：把业务事件（Inbound 消息、OutReach 请求）翻译成标准化的 ClientTaskJob
2. **排期**：通过 `resolvePlannerDueAt` 计算工作时间窗口——WhatsApp 只能白天发、避开节假日和商家休息时间
3. **去重/串行**：通过 `CancelOtherJobsOnInbound` 保证同一商家同一时间只有一个活跃 Job

### 链路

```
Inbound（商家发消息）：
  → Planner.PlanInbound
     → 创建 Inbound Job（taskName = TaskNameInboundMessage）
     → CancelOtherJobsOnInbound（取消该商家所有待执行的 OutReach、Strategy 等 Job）
  → Dispatcher.DispatchDue（轮询 Redis Sorted Set 出队 → 抢租约 → 分发）
  → Engine.ProcessTask

OutReach（主动触达）：
  上游触发（StrategyExecution 到期 / 手动调用 / 定时规则）：
  → Planner.PlanOutreachReportPrep（先创建"准备报告"的 Job）
     → OutreachReportPrep Executor 执行：准备商家数据
     → 执行完成后调用 Planner.PlanOutreach（创建实际的触达 Job）
  → Dispatcher.DispatchDueByTaskNames（轮询 Redis Sorted Set 出队 → 抢租约 → 渠道账号轮询）
  → Engine.ProcessTask

PostInbound（回复后跟进）：
  → Inbound 执行完后自动调用 Planner.PlanPostInbound
     → 创建一个 TaskTracing 任务（决定是否跟进推销）
```

**关键机制：**
- `CancelOtherJobsOnInbound`：商家回复时，自动取消该商家所有待执行的主动触达任务，保证同一商家串行且不被打扰
- `resolvePlannerDueAt`：根据渠道规则（WhatsApp 需在工作时间）和节假日，计算 Job 的执行时间
- `TaskKey` 幂等：Planner.CreateIfNotExists 保证同一个 TaskKey 的 Job 不会被重复创建
- **调度队列优化**：详见下文「调度队列优化设计」

### Planner vs 触达策略

"什么时候该找谁聊"是触达策略的职责，不在 Planner 中。触达策略由上游的 `ClientStrategyExecution` 体系负责：

- Strategy 是独立的业务层，不在 Agent 设计范围内
- Strategy 产生 `NextActionAt` 时间戳
- Planner 只负责接收这个时间戳并创建对应的 Job
- 如果只有一个简单的定时规则（如"沉默 7 天后触达"），可以通过外部定时器直接调用 `Planner.PlanOutreach`

### 调度队列优化设计：Redis Sorted Set 调度前端

#### 动机：为什么需要优化

在初始设计中，MySQL 同时承担了两个职责：**持久化存储** 和 **调度队列**。这样做简单可靠，但在生产环境下暴露了三个问题：

| 问题 | 原因 | 后果 |
|-|-|-|
| **索引热力点** | Dispatcher 每几秒扫一次 `idx_due_status`，所有实例都落在 `status=PENDING` 的同一段索引上 | B+ Tree 索引页竞争 |
| **调度时效性天花板** | 轮询间隔缩短 1 倍，MySQL 压力增加 1 倍 | 不敢缩短轮询间隔 |
| **职责混杂** | 一张表同时承担存储、扫描、排序三种职责 | 难以独立优化 |

优化思路：**把调度职责从 MySQL 剥离到 Redis，MySQL 退化为纯持久化存储。**

#### 架构变化

```
优化前（v1）：
  Planner → INSERT MySQL（存储 + 队列）← Dispatcher 轮询扫描

优化后（v2）：
  Planner → INSERT MySQL（存储） → 异步同步 → Redis Sorted Set（调度索引） ← Dispatcher 轮询 Redis
```

#### Redis Key 设计

调度层只用三个 Redis Key：

| Key | 类型 | 用途 | 生命周期 |
|-|-|-|-|
| `job:due:queue` | ZSET | 调度队列，member=jobId，score=dueAt 时间戳 | 永久（ZADD/ZREM/DEL 管理） |
| `job:status:{jobId}` | STRING | 状态缓存，"PENDING" / "DONE" / "CANCELLED" / "FAILED" | TTL = max(3600, dueAt - now + 3600) 秒 |
| `job:lease:{jobId}` | STRING | 分布式租约，value=instanceId | TTL = 30 秒（自动过期） |

**注意：** `job:status:{jobId}` 不保存 LEASED / PROCESSING 等中间态——它只反映最终状态。中间态由 MySQL 的 status 字段和 Redis 的租约机制共同管理。

#### Planner 完整流程

```
Planner 创建 Job：
  Step 1（同步）：MySQL INSERT（唯一真相源）✅
  Step 2（异步，毫秒级）：Lua 脚本原子写入 Redis
    → ZADD job:due:queue {score=dueAt} {member=jobId}
    → SETEX job:status:{jobId} {ttl=动态计算} "PENDING"

Planner 取消 Job：
  Step 1（同步）：MySQL UPDATE status = CANCELLED ✅
  Step 2（异步，毫秒级）：Lua 脚本原子写入 Redis
    → ZREM job:due:queue {jobId}
    → SET job:status:{jobId} "CANCELLED"
```

为什么用 Lua 脚本代替 Pipeline？因为 Pipeline 不是原子操作——ZADD 成功但 SETEX 失败会导致 Job 在 ZSET 中但没有状态缓存。Lua 脚本保证要么全成功，要么全失败。

#### Dispatcher 完整流程

```
Dispatcher（轮询 Redis，不碰 MySQL）：
  →
  1. ZRANGEBYSCORE job:due:queue 0 {now()} WITHSCORES LIMIT 0 100
     → 取出到期的 jobId 列表

  2. for each jobId:
       GET job:status:{jobId}
       ├── "CANCELLED" / "DONE" / "FAILED"
       │   → ZREM job:due:queue {jobId}（清理脏数据）
       │   → continue
       │
       ├── "PENDING"
       │   → SET NX EX 30 job:lease:{jobId} {instanceId}
       │     ├── 成功 → 抢到租约 → 异步交给 Engine 处理
       │     └── 失败 → 已被其他 Engine 抢走 → continue
       │
       └── null（缓存过期，概率极低）
           → SET NX EX 30 job:lease:{jobId} {instanceId}
             ├── 成功 → 抢到租约 → 交给 Engine（Engine 自己去 MySQL 确认状态）
             └── 失败 → continue

  3. Redis 不可用 → 降级到 MySQL 轮询（回退 v1 模式）
```

#### Engine 执行前流程

```
Engine 收到 jobId：
  → SELECT * FROM client_task_job WHERE id = {jobId}（按主键点查，无索引热）
  → 双检查 status：
    ├── 是 PENDING → 正常执行
    │     执行完后：
    │       MySQL UPDATE status = DONE
    │       Redis SET job:status:{jobId} "DONE"
    │       Redis DEL job:lease:{jobId}
    │       Redis ZREM job:due:queue {jobId}
    │
    └── 已是 DONE / CANCELLED → Redis 脏了
        Redis SET job:status:{jobId} {MySQL 里的实际状态}
        Redis ZREM job:due:queue {jobId}
        跳过执行
```

**注意：** Engine 本来就要从 MySQL 捞全量数据进行处理（TaskPayload 组装），双检查是顺带做的，不是额外开销。

#### 数据一致性：三层兜底

| 层级 | 机制 | 对齐时间 | 兜底什么 |
|-|-|-|-|
| **第一层（实时）** | Lua 脚本原子写入，失败后本地队列重试 | 毫秒~秒级 | 正常情况下 Planner 写 Redis 失败 |
| **第二层（执行时）** | Engine 从 MySQL 捞数据时双检查状态 | 触发时纠正 | Redis 状态和 MySQL 不一致 |
| **第三层（Safety Net）** | 定时任务每 10 分钟重建 Redis 队列 | 10 分钟内完全对齐 | 上述两层全部失效 |

第三层重建逻辑：

```
每 10 分钟（Redis 正常时才执行）：
  1. 修复僵死 LEASED：
     UPDATE client_task_job SET status=PENDING, lease_holder=NULL
     WHERE status='LEASED' AND lease_expire_at < NOW()
  2. 捞待处理 Job：
     SELECT * FROM client_task_job
     WHERE (status='PENDING' OR status='LEASED') AND due_at <= NOW()
  3. 全量覆盖 Redis：
     DEL job:due:queue
     Pipeline 批量 ZADD + SETEX（for each job）
```

**为什么要扫 LEASED + 租约过期？** 防止 Engine 崩溃后 Job 永远卡在 LEASED 状态没人处理。

#### 优化效果对比

| 维度 | v1（纯 MySQL） | v2（Redis Sorted Set） |
|-|-|-|
| **Dispatcher 扫描目标** | MySQL 表（磁盘/内存 B+ Tree） | Redis ZSET（纯内存，O(log N)） |
| **MySQL 读压力** | 每 N 秒扫一次全量 PENDING | 只有 Engine 执行前按 ID 点查 |
| **调度时效性** | 受限于 MySQL 轮询间隔（通常 5s+） | 可以降到亚秒级 |
| **MySQL 负载特征** | 与调度频率成正比（固定开销） | 与业务量成正比（弹性） |
| **Redis 挂了** | 不受影响 | 降级到 v1 |
| **Redis 内存开销** | 无 | 百万级 Job 约 120MB |

## RequestPreparer 请求准备层

Engine 调用 Agent 之前，需要先把原始 TaskPayload 解析成干净的 AgentRequest。这个职责由 RequestPreparer 承担。

### 设计原则

> RequestPreparer 只负责"谁、在哪儿、用什么语言"——这是运行环境上下文。Agent 内部的 Context 组装负责"需要查什么业务数据"——这是推理上下文。两者不重叠。

### 接口

```java
public interface RequestPreparer {
    AgentRequest prepare(Task task);
}
```

### 职责范围

| 问题 | 数据来源 | 产出 |
|------|---------|------|
| 商家是谁？ | CRM / 渠道绑定 | userId, accountId |
| 什么类型的任务？ | 入口类型 | taskType: INBOUND / OUTREACH |
| 说什么语言？ | 商家偏好 | languageCode: en / th / id |
| 什么渠道？ | 渠道绑定 | channelId: whatsapp / email, channelAccountId |
| 当前时间？ | 系统时钟 | timezone: Asia/Bangkok |
| 触达策略是什么？ | StrategyExecution | 策略摘要（阶段、目标、约束） |

**这段不做的事：**
- ❌ 不查广告投放效果（那是 Tool 的事）
- ❌ 不查优惠政策（那是 Tool 的事）
- ❌ 不组装记忆（那是 Agent 内部的事）

### 触达策略注入（方案 C）

触达策略（当前是第几次触达、目标是什么、有什么约束）通过 RequestPreparer 预注入 System Prompt：

```
RequestPreparer 准备 AgentRequest 时：
  1. 查询当前商家的触达策略（阶段、目标、约束）
  2. 注入 System Prompt 的策略块
  3. Agent 默认知道策略，不需要额外 Tool 调用

Agent 处理过程中如果产生策略相关的变化信号（例：商家说"别再推销了"）：
  → 调 Tool 重新查当前策略，确认是否需要更新约束
```

System Prompt 中的策略段示例：

```
[触达策略]
  阶段: 第 3 次触达
  目标: 唤醒沉默商家
  约束: 不要主动提及折扣
  渠道: whatsapp（channelAccountId: wa_biz_01）
```

- 默认零额外延迟：策略摘要随 AgentRequest 一起注入
- 必要时自己查：Agent 检测到策略可能变化时，通过 Tool 重新确认

### RequestPreparer vs 旧版 Enhancer

ai-sales 旧版有 15+ 种 Enhancer 在进 Agent 之前就把业务数据查好。在本设计中，**Enhancer 职责缩小为 RequestPreparer**，只做环境解析，不做业务预取。Agent 需要什么数据，自己在 ReActLoop 中通过 Tool 查询。

---

## 七、Context 组装与写回机制

### 每次 Session 开始时如何组装 Context

每次新的 `Session` 开始时，系统都按固定顺序组装 Context。

```text
组装 Context（按顺序）

1. 定位当前对象
   Story（user_id） -> Conversation（conversation_id） -> Session（new session_id）

2. 捞取记忆
   a. 从 Conversation 拿：近 N 轮完整摘要 + 早期轮次的合并密集摘要
   b. 从 Story 拿：用户画像 + 历史 Conversation 摘要索引
   c. 从 KV/DB 拿：结构化业务数据（标签、订单、库存、权益等）
   d. 从 Vector Store 拿：语义相关知识、FAQ、产品资料

3. 两端式构建运行态 Context（解决 lost-in-the-middle）

   [第一段：开头——高优先级]
   a. System Prompt（角色定义、回复风格、安全合规、工具使用规则）
   b. 当前 Session 输入（最新用户消息 + metadata）
   c. Available Tools（经 L2-L4 裁剪后的可见 Tool 定义）

   [第二段：中间——可压缩区]
   d. Story 画像 + 历史摘要索引
   e. Conversation：最近 N 轮完整摘要 + 早期轮次合并密集摘要
   f. KV/DB 业务事实（按与当前问题的相关性排序）
   g. Vector Store 语义知识（按相关性分数排序）

   [第三段：结尾——高优先级]
   h. 本轮 Tool 最新调用结果
   i. 当前正在处理的 thought/observation（ReActLoop 最新状态）
   j. Output Policy（输出约束、是否允许追问、是否优先使用工具）

4. 进入 ReActLoop 推理
```

### 为什么按这个顺序

业界研究（Lost in the Middle）表明 LLM 对 Context 的开头和结尾注意力更高，中间部分容易衰减。因此本系统做了两端式排序：

- **开头放**：当前输入 + 工具定义——模型需要立刻知道的
- **结尾放**：Tool 最新结果 + 当前推理状态——模型刚拿到、正要处理的信息
- **中间放**：历史记忆 + 结构化事实——已经沉淀过的、不需要最高注意力的信息

### Conversation 内部双层摘要（配合两端式）

为了不让 Conversation 历史过度膨胀并占据 Context 中间位置，Conversation 内部做分层管理：

```text
Conversation.summary 结构：
  ├── recentSessions: List<SessionSummary>    ← 最近 3-5 轮，保持原始摘要格式
  └── earlySummary: String                    ← 更早的轮次合并成一段稠密文本
```

- Session 数量 <= 5 时：全部保持原始摘要
- Session 数量 > 5 时：`earlySummary` 合并旧轮次，`recentSessions` 保持最近 5 轮

注意：这不改变整体的 Context Token 策略 C（全塞，超长截断重试）。两端式排序和双层摘要是对策略 C 的补充优化，不额外增加每轮动态计算的开销。

### Context 的推荐结构

为了让模型稳定消费，建议把 Context 组织成明确的块，而不是一团 message history。

```markdown
# System
- 角色定义
- 回复风格
- 安全与合规要求
- 工具使用规则

# Current Session
- user_id
- conversation_id
- session_id
- current user input

# Conversation Memory
- 当前对话摘要
- 当前任务状态
- 已确认事实
- 本轮临时偏好

# Story Memory
- 用户画像
- 长期偏好
- 历史重要事件
- 历史 Conversation 摘要索引

# Business Facts
- KV/DB 查询结果
- 订单/库存/标签/权限/权益等结构化数据

# Knowledge Retrieval
- FAQ
- 产品知识
- 命中的文档片段

# Available Tools
- tool definitions
- tool constraints

# Output Policy
- 是否允许追问
- 是否优先使用工具
- 是否输出结构化结果
```

这样做有几个好处：

- 模型知道每块信息的来源和可信度
- hooks 可以对不同 block 做精确裁剪和注入
- 后续做压缩、审计、观测都更容易

---

## 八、Hooks 治理体系

如果说 `ReActLoop` 负责主干执行，那么 `Hooks` 负责在不污染主流程的前提下做扩展。

建议 Hooks 只做四类事：

- 观测：日志、埋点、trace、metrics
- 注入：补充 context、补充策略、补充工具白名单
- 治理：审计、风控、敏感词、权限校验
- 写回：触发摘要、记忆提炼、事件沉淀

### 推荐 Hook 生命周期

```text
onSessionStart
onContextAssembling
onMemoryRetrieved
onBeforeModelCall
onAfterModelCall
onBeforeToolCall
onAfterToolCall
onBeforeSessionSummarize
onConversationUpdated
onStoryUpdated
onSessionEnd
onError
```

### 各 Hook 的职责建议

#### `onSessionStart`

负责：

- 初始化 traceId、sessionId
- 打点本次请求开始
- 记录入口参数

#### `onContextAssembling`

负责：

- 在 Context 完成前注入额外 system policy
- 控制不同来源 memory block 的 token 配额
- 根据场景动态启用或禁用部分 tool

#### `onMemoryRetrieved`

负责：

- 对从 `Conversation`、`Story`、`KV/DB`、`Vector Store` 捞到的信息做裁剪
- 给不同来源打标签，便于后续审计和解释
- 去重和排序

#### `onBeforeModelCall` / `onAfterModelCall`

负责：

- 记录 prompt 大小、召回内容、模型耗时
- 做输出检查
- 识别是否需要强制工具调用或强制追问

#### `onBeforeToolCall` / `onAfterToolCall`

负责：

- 工具鉴权
- 参数校验
- 超时控制
- 结果裁剪
- 工具结果标准化

#### `onBeforeSessionSummarize`

负责：

- 把本轮 Session 里真正高价值的信息提炼出来
- 过滤掉中间推理噪音
- 产出用于写回 `Conversation` 的 summary + state delta

#### `onConversationUpdated`

负责：

- 将本轮摘要并入 Conversation
- 更新当前任务状态
- 触发异步持久化

#### `onStoryUpdated`

负责：

- 判断哪些信息已经具备长期稳定性
- 更新用户画像
- 记录新的历史摘要索引

### Hook 设计原则

- Hook 不改主语义，只做增强
- Hook 应尽量无状态或弱状态
- Hook 的失败不应轻易拖垮主流程
- Hook 的输入输出要可观测、可审计

---

## 九、Tool 查询能力体系

这个 Agent 不是纯聊天机器人，而是要通过 `Tool` 访问真实世界能力。

但这里要加一个非常重要的前提：

> 这个系统里的 Tool 只有查询语义，没有写入语义。

所以 `Tool` 的定位不是"外挂功能"，而是 ReActLoop 中用于"查询外部事实"的标准执行器，而不是命令执行器。

### Tool 的职责

每个 Tool 负责一类明确能力，例如：

- 查询订单
- 查询库存
- 查询价格
- 获取知识库详情
- 查询门店信息
- 查询知识片段

每个 Tool 应该至少包含：

- `name`
- `description`
- `schema`
- `execute()`
- 权限要求
- 超时与失败策略

在这个前提下，Tool 机制的设计目标也会更聚焦：

- 核心目标是拿到真实、及时、可校验的外部事实
- 不承担状态变更，不承担业务写入
- 安全边界天然更清晰
- ReActLoop 不需要处理"是否允许执行写操作"这类高风险决策

但如果只停在这几个字段，Tool 机制仍然不够可实现。  
真正可落地的 Tool 设计，至少要回答下面几个问题：

- 模型如何知道什么时候该用这个 Tool
- Tool 参数如何定义、校验、纠错
- Tool 调用失败后如何重试、降级、回退
- Tool 返回什么结构，才能被后续推理稳定消费
- Tool 的权限、租户、用户身份如何透传
- 哪些 Tool 可以并发，哪些必须串行
- Tool 的结果是直接回用户，还是先进入下一轮推理

也就是说，Tool 不是一个简单的 `execute()` 接口，而是一整套"可被 LLM 调度的外部能力协议"。

### Tool 元数据设计

建议每个 Tool 不只暴露函数签名，还要暴露一份完整元数据。

最少应包含：

| 字段 | 说明 |
|------|------|
| `name` | 全局唯一名称，供模型和调度器引用 |
| `description` | 给模型看的用途说明，告诉模型什么时候该用 |
| `inputSchema` | 参数结构定义，建议 JSON Schema 风格 |
| `outputSchema` | 返回结构定义，便于后续 observation 标准化 |
| `sideEffectLevel` | 副作用级别；当前系统固定为 `READ_ONLY` |
| `authPolicy` | 需要什么身份、权限、租户上下文 |
| `timeoutMs` | 单次调用超时 |
| `retryPolicy` | 是否重试、重试次数、退避策略 |
| `idempotent` | 是否幂等，影响重试安全性 |
| `visibility` | 是否对当前场景可见，例如只在某些业务域暴露 |
| `resultPolicy` | 返回结果是否截断、脱敏、摘要化 |

在只读约束下，建议把 Tool 分成两类：

| 类型 | 特征 | 例子 |
|------|------|------|
| `query tool` | 只读、低风险、可重试 | 查库存、查订单、查 FAQ |
| `decision support tool` | 返回分析材料，但本质仍然是只读查询 | 方案推荐、比价、规则解释 |

这样分类的好处是，调度策略可以天然不同：

- `query tool` 可以更积极地自动调用
- `decision support tool` 可以先给模型做分析，再组织回复

这个分类比"查询 / 写入 / 命令"更适合你们当前系统，因为它准确反映了一个事实：

> 不管返回的是原始事实还是分析材料，本质上都仍然是只读查询。

### Tool 接口建议

建议把 Tool 的接口从"只有执行函数"提升为"定义 + 执行上下文 + 标准结果"的形式。

```java
public interface Tool {
    String getName();
    String getDescription();
    ToolDefinition getDefinition();
    ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments);
}
```

其中需要补出两个关键对象：

#### ToolExecutionContext

它不是业务记忆对象，而是本次工具调用的运行上下文，建议包含：

- `userId`
- `conversationId`
- `sessionId`
- `traceId`
- `tenantId`
- `requestTime`
- 当前权限信息
- 当前语言、时区、地区
- 调用来源，例如用户触发还是系统补全

这个对象的作用是统一透传身份和治理信息，避免每个 Tool 自己拼这些字段。

#### ToolResult

Tool 的返回值不要只用一个字符串。  
建议标准化为结构化结果，例如：

```java
public class ToolResult {
    private boolean success;
    private String code;
    private String message;
    private Object data;
    private boolean retryable;
    private boolean userVisible;
    private String summary;
}
```

这样做的好处是：

- `ReActLoop` 能区分成功、失败、可重试、不可重试
- Hook 能对结果做标准化审计
- 模型能拿到一份更干净的 `observation`
- 前端或 API 层也更容易决定展示策略

### ToolRegistry 与调度层

Tool 设计不能只停留在单个 Tool 本身，还必须有一个统一的注册与调度层。

建议引入 `ToolRegistry`，负责：

- 注册全部 Tool
- 根据场景筛选当前可见 Tool
- 通过名称查找 Tool
- 返回给模型的 tool definitions
- 管理工具版本和别名

在 `ToolRegistry` 之上，还建议有一个 `ToolDispatcher` 或 `ToolExecutor`，负责：

- 参数反序列化
- 参数校验
- 权限检查
- 超时控制
- 重试与熔断
- 执行结果标准化
- 异常转成统一 `ToolResult`

也就是说职责要拆开：

- `Tool`：只关心业务能力本身
- `ToolRegistry`：只关心有哪些 Tool
- `ToolDispatcher`：只关心怎么安全稳定地执行 Tool

如果不这么拆，问题会很快出现：

- 每个 Tool 都自己做权限校验，逻辑分散
- 每个 Tool 都自己做异常包装，风格不一致
- 每个 Tool 都自己判断超时、重试，治理能力无法统一

### 分层注册

我认为需要，而且应该明确设计成"分层注册 + 分层筛选"的机制。

但这里要注意一个边界：

> 分层的是 Tool 的注册与暴露机制，不是再新增一套业务对象模型。

也就是说，`Tool` 仍然只是 Tool；  
只是 `ToolRegistry` 不能是一个简单的平铺 `Map<String, Tool>`，否则一旦 Tool 数量上来，就会出现几个问题：

- 很难按业务域管理
- 很难按环境和权限裁剪
- 很难控制不同 Session 看到的 Tool 集
- 很难做灰度、版本、别名、下线治理
- 很难解释"为什么这个 Tool 当前可见/不可见"

所以，注册机制最好天然分层。

#### 建议的四层注册模型

推荐把 Tool 注册机制设计成下面四层：

| 层级 | 作用 | 是否持久存在 |
|------|------|--------------|
| `L1: Base Registry` | 注册系统中全部 Tool 原子定义 | 是 |
| `L2: Domain Registry` | 按业务域、能力域组织 Tool 集合 | 是 |
| `L3: Policy Filter` | 按环境、角色、租户、权限做筛选 | 否，运行时计算 |
| `L4: Session Visible Set` | 针对当前 Session 暴露给模型的最终 Tool 集 | 否，运行时计算 |

这四层分别解决的是四类不同问题。

##### L1: Base Registry

这一层是最底层的"全量工具事实表"。

它只回答一个问题：

> 系统里到底有哪些 Tool。

这一层存的是每个 Tool 的基础元数据：

- `name`
- `alias`
- `description`
- `inputSchema`
- `outputSchema`
- `domain`
- `tags`
- `authPolicy`
- `timeoutMs`
- `retryPolicy`

这一层不处理场景，不处理当前用户，也不处理当前对话。  
它只是一个全局注册中心。

可以把它理解成：

- 类似 Spring 里的 Bean 注册表
- 类似 API Gateway 里的接口目录
- 类似能力平台里的能力清单

##### L2: Domain Registry

这一层解决的是"工具太多以后，如何按业务域组织"的问题。

比如可以按下面方式分组：

- `order-tools`
- `inventory-tools`
- `product-tools`
- `faq-tools`
- `after-sale-tools`

也可以进一步按能力标签组织：

- `query-basic`
- `query-sensitive`
- `knowledge-retrieval`
- `analysis-support`

这一层的作用不是执行鉴权，而是做"结构化编组"。

好处很明显：

- 便于维护和运营
- 便于按域启停
- 便于做场景化组合
- 便于未来做版本迁移和灰度替换

例如：

- 销售场景默认挂 `product-tools + inventory-tools + faq-tools`
- 售后场景默认挂 `order-tools + after-sale-tools + faq-tools`

这样就不是每次从几十个 Tool 里硬筛，而是先从域级别收敛一次。

##### L3: Policy Filter

这一层是运行时治理层。

它基于当前请求上下文做动态过滤，例如：

- 当前环境是不是生产
- 当前用户是否登录
- 当前角色是不是客服
- 当前租户是否有这个能力
- 当前请求是否命中敏感查询范围
- 当前会话是否已经具备必要前置参数

这层输出的不是最终给模型的结果，而是：

> 当前请求在治理上允许访问哪些 Tool。

所以这一层更像一个运行时策略裁剪器。

##### L4: Session Visible Set

这是最贴近模型的一层。

它要回答的问题是：

> 在"系统存在"且"治理允许"的 Tool 里，这一轮 Session 真正要暴露给模型哪些。

这一层通常还会结合：

- 当前用户问题意图
- 当前 `Conversation` 状态
- 当前可用 token budget
- 当前模型能力
- 当前是否需要降低选择复杂度

例如：

- 用户在问"订单物流"，本轮不必把库存、门店、FAQ 的所有 Tool 都暴露给模型
- 用户在问"某车型配置对比"，可以只暴露产品和知识检索相关 Tool

这一层的核心目标是降低模型选择成本，减少误调。

#### 为什么不能只有一层注册

如果只有一层扁平注册，常见实现通常是：

```java
Map<String, Tool> allTools;
```

这在 demo 阶段够用，但很快会出现问题：

1. 所有 Tool 都堆在一起，难以按域管理
2. 可见性逻辑会散落在多处 if/else 里
3. 某个场景需要的 Tool 集无法稳定复用
4. 很难解释模型为什么看到了某些 Tool
5. 很难做分环境、分租户、分角色控制

所以我更建议：

> Base Registry 解决"存在性"，Domain Registry 解决"组织性"，Policy Filter 解决"合规性"，Session Visible Set 解决"可用性"。

#### 分层注册的执行链路

把这套机制串起来，运行时可以这样走：

```text
all tools in Base Registry
   ->
select domain tool sets
   ->
apply policy filter
   ->
apply session/context filter
   ->
get final visible tools
   ->
serialize tool definitions to model
```

如果进一步展开，可以是：

```text
1. Base Registry 返回系统全量 Tool
2. Domain Registry 根据业务场景选出候选集合
3. Policy Filter 根据环境/角色/权限/租户做裁剪
4. Session Visible Set 根据当前问题和 Conversation 状态再次裁剪
5. ReActLoop 把最终 Tool 集交给模型
6. 模型只在这个最小可见集合里做选择
```

#### 各层分别放在哪里

结合当前 YHarness 的结构，我建议这样落：

| 层级 | 建议代码位置 |
|------|--------------|
| `Base Registry` | `tools/registry/` |
| `Domain Registry` | `tools/catalog/` 或 `tools/registry/` |
| `Policy Filter` | `tools/policy/` 或 `hooks/` |
| `Session Visible Set` | `context/` + `core/` |

这里可以看出一个关键点：

- 注册是 `tools` 域里的事情
- 最终暴露给模型，是 `context` 组装阶段的事情
- 执行前再做治理，可以由 `dispatcher` 和 `hooks` 共同承担

所以分层注册不是一个孤立组件，而是横跨：

- `tools`
- `context`
- `core`
- `hooks`

#### 分层注册后的核心接口建议

建议不要让 `ToolRegistry` 只暴露一个 `getAllTools()`，而是显式支持分层查询。

例如：

```java
public interface ToolRegistry {
    Collection<ToolDefinition> getAllTools();
    Collection<ToolDefinition> getToolsByDomain(String domain);
    Collection<ToolDefinition> getToolsByTags(Set<String> tags);
    Optional<Tool> findByName(String name);
}
```

然后再单独加一层可见性解析器：

```java
public interface ToolVisibilityResolver {
    List<ToolDefinition> resolveVisibleTools(AgentContext context);
}
```

这样职责会更清楚：

- `ToolRegistry` 负责注册事实
- `ToolVisibilityResolver` 负责运行时裁剪
- `ToolDispatcher` 负责执行

#### 是否会让设计变复杂

会增加一点机制复杂度，但这是值得的。

因为它换来的是：

- Tool 数量增长后的可维护性
- 不同业务场景的稳定复用能力
- 更好的安全治理
- 更低的模型误选概率
- 更清晰的解释能力

而且这个复杂度不是业务复杂度，而是平台治理复杂度。  
对于 Agent 框架来说，这种复杂度是值得提前设计好的。

#### 最终建议

所以我的结论是：

> Tool 需要分层注册，而且应该把"注册、编组、裁剪、执行"明确拆开。

更具体地说：

- 用 `Base Registry` 管全量 Tool
- 用 `Domain Registry` 管场景和业务域编组
- 用 `Policy Filter` 管权限、环境、租户、合规
- 用 `Session Visible Set` 管当前这一轮真正暴露给模型的最小 Tool 集

这套机制会比单层 `ToolRegistry` 更适合生产级 Agent，尤其适合你们这种"只读查询型 Tool"体系，因为它天然强调的是：

- 可控暴露
- 最小授权
- 场景收敛
- 安全查询

### Tool 选择策略

模型不是看到 Tool 就一定能用好，所以还需要明确"何时暴露、何时调用、何时禁止"。

建议至少有三层选择机制：

#### 静态白名单

按业务域、角色、环境过滤 Tool。

例如：

- 未登录用户不暴露订单查询 Tool
- 非客服角色不暴露用户权益查询 Tool
- 非特定业务域不暴露专属知识查询 Tool

#### 动态白名单

根据当前 `Conversation` 状态动态暴露 Tool。

例如：

- 未拿到订单号前，不暴露订单详情 Tool
- 未确定商品 ID 前，不暴露库存明细 Tool
- 当前问题与售后无关时，不暴露售后知识查询 Tool

#### 模型内决策

在前两层约束后的可见范围内，由模型根据当前问题选择具体 Tool。

所以 Tool 的"选择权"不是完全交给模型，而是：

> 先由系统治理层决定"能不能看见"，再由模型决定"要不要调用"。

### Tool 参数设计

很多 Agent 在 Tool 失败上，核心问题不是执行失败，而是参数设计太弱。

建议参数设计遵循下面原则：

- 参数名业务语义明确，不要抽象成 `value1`、`paramA`
- 必填参数必须明确
- 枚举值必须收敛，不要允许任意自由输入
- 能拆的复杂对象尽量拆成结构化字段
- 对用户口语输入常见歧义给出说明

例如比起：

```json
{
  "query": "查一下这个"
}
```

更好的定义是：

```json
{
  "orderId": "123456",
  "queryType": "detail"
}
```

同时建议支持一层参数纠错逻辑：

- 缺少关键参数时，优先让模型追问
- 参数格式轻微错误时，可做安全修正
- 存在高风险歧义时，禁止自动修正，必须追问

### Tool 返回值设计

Tool 的结果既不能太原始，也不能过度加工。

建议至少分成三层：

1. 原始数据 `rawData`
2. 结构化结果 `data`
3. 给模型消费的简短摘要 `summary`

例如库存查询结果可以是：

- `rawData`：完整库存接口响应
- `data`：车系、门店、数量、更新时间
- `summary`：`北京朝阳店当前有现车 5 台，可预约到店`

这样设计的原因是：

- 原始数据便于排查
- 结构化数据便于后续程序处理
- 摘要便于模型直接吸收进 observation

### Tool 错误处理设计

Tool 机制最容易被低估的部分就是错误处理。

建议把错误分成四类：

| 类型 | 处理方式 |
|------|----------|
| 参数错误 | 不重试，优先追问用户或让模型修正参数 |
| 权限错误 | 不重试，直接拒绝并记录审计 |
| 临时错误 | 可重试，例如超时、瞬时网络失败 |
| 业务错误 | 通常不重试，例如库存不存在、订单不存在 |

建议 `ToolDispatcher` 统一把异常映射成标准结果，例如：

- `INVALID_ARGUMENT`
- `PERMISSION_DENIED`
- `TIMEOUT`
- `RATE_LIMITED`
- `UPSTREAM_UNAVAILABLE`
- `BUSINESS_NOT_FOUND`
- `DATA_NOT_READY`

这样 `ReActLoop` 才能做稳定决策：

- 该追问
- 该重试
- 该换 Tool
- 该直接回复用户失败原因

### Tool 的安全模型

因为所有 Tool 都只有查询语义，所以安全模型可以收敛成"只读工具安全"。

建议把安全约束明确写死：

| 约束 | 说明 |
|------|------|
| `READ_ONLY` | 所有 Tool 不允许修改外部状态 |
| `NO_SIDE_EFFECT` | 所有 Tool 不触发扣费、下单、发消息、发工单等副作用 |
| `AUDITABLE` | 所有 Tool 调用都保留 trace 和审计日志 |
| `SCOPED_AUTH` | 所有 Tool 只能在授权范围内查询 |

这样做的好处是：

- 模型不会获得任何"执行命令"的能力
- 调度层不需要处理写入保护和确认流
- Hook 不需要承担写操作风控
- 整个 Agent 更容易通过安全评审

这也是你说"这才是足够安全的"的核心原因：

> 一旦 Tool 不具备写入语义，Agent 的风险面就会从"执行型风险"收缩为"查询型风险"。

查询型风险仍然存在，但主要集中在：

- 越权查询
- 敏感信息泄露
- 错误参数导致的误查
- 高频调用导致的资源滥用

这些风险都比写操作风险更容易治理。

### Tool 与 ReActLoop 的协作深度

Tool 在 ReActLoop 中不只是"调一下然后拿结果"，更重要的是定义 observation 的质量。

建议 Tool 调用后，ReActLoop 至少要做三件事：

1. 把 `ToolResult.summary` 注入 observation
2. 把关键结构化字段写入 session state
3. 判断是否需要继续下一轮 query tool call

例如：

- 查询订单详情后，下一轮可能继续查询物流信息
- 查询库存失败后，下一轮应该追问商品 ID，而不是盲重试

所以 Tool 返回结果不是终点，而是下一轮推理的输入材料。

### Tool 与 Hook 的边界对比

这是最容易混淆的一组边界。

| 机制 | 负责什么 | 不负责什么 |
|------|----------|------------|
| `Tool` | 执行具体查询能力 | 生命周期治理、全局观测 |
| `Hook` | 执行前后注入治理、观测、审计 | 具体业务逻辑本身 |

举例：

- "查订单详情" 是 `Tool`
- "查订单前校验权限" 可以由 `Hook` 做统一增强，也可以由 dispatcher 做统一治理
- "记录这次查订单耗时" 是 `Hook`
- "把订单响应转成 observation" 属于 Tool result 标准化链路

一句话区分：

> Tool 是干活的，Hook 是围绕干活过程做治理和扩展的。

### Tool 与 Provider Function Calling 的边界对比

还需要再区分一层：`Tool` 和大模型原生 `function calling` 不是一个概念。

| 概念 | 角色 |
|------|------|
| `Tool` | 业务能力抽象 |
| `function calling` | 模型和外部能力通信的一种协议 |

也就是说：

- 你可以用 OpenAI function calling 来承载 Tool 调用
- 也可以用 Anthropic tool use 来承载 Tool 调用
- 甚至也可以自己解析文本 action 来调 Tool

所以：

> function calling 是协议层，Tool 是能力层。

这层区分很重要，因为这样你的 Tool 机制不会被某个模型厂商绑定死。

### Tool 与 Memory 的边界对比

很多系统会把"去数据库查用户偏好"和"从 Story 里拿用户偏好"混为一谈。

建议严格区分：

| 来源 | 代表什么 |
|------|----------|
| `Story / Conversation` | Agent 自己保留的历史上下文 |
| `Tool + KV/DB` | 外部系统的当前事实 |
| `Tool + Vector Store` | 外部知识系统的语义召回 |

区分不只在来源，更在时效性：

- `Memory` 更强调历史连续性
- `Tool` 更强调当前事实性

如果用户问"我刚才说预算多少"，优先看 `Conversation`。  
如果用户问"这台车现在还有没有货"，必须走 `Tool`。

### Vector Store 召回策略

Vector Store 在 Tool 体系中属于外部语义召回。它的使用策略直接决定了 Agent 获取知识的质量。本系统采用**两级召回策略**：

#### 召回策略（方案 C）

```
Tool searchKnowledge 执行时：
  1. 查询 Vector Store，按语义相似度召回 top-10 chunk
  2. 按分数分两级：
     - 高相关（score > 0.7）：直接进入 Context 正文区
     - 低相关（score 0.4-0.7）：作为附加上下文，放在 Context 末尾
  3. score < 0.4：不入 Context
```

#### 召回结果的结构化返回

```json
{
  "highRelevance": [
    {"content": "标准优惠方案是首次投放返现 10%...", "score": 0.92, "source": "policy_v3"},
    {"content": "美妆行业客户可申请额外素材补贴...", "score": 0.88, "source": "policy_v3"}
  ],
  "lowRelevance": [
    {"content": "2023年Q4的促销活动已经结束...", "score": 0.55, "source": "faq_archive"}
  ]
}
```

#### 两级分区的 Context 设计

```
Context 正文区（高相关知识）：
  → 放在 KV/DB 事实之后、Tool 最新结果之前
  → 模型正常推理时，这些知识是可见的

Context 附录区（低相关知识）：
  → 放在 Context 最末尾、Output Policy 之前
  → System Prompt 附带一句："Context 末尾附有低相关的参考信息，如果正文信息不足时可参考"
```

这样做的理由是：不需要在每次推理前做精确的 token 预算计算，用两级阈值即可自然控制召回量。score > 0.7 的高相关 chunk 是"生产数据"，score 0.4-0.7 的是"参考数据"——模型视需要决定是否使用。

### Tool 编排策略对比

Tool 调用至少有三种常见编排方式：

| 方式 | 特点 | 适用场景 |
|------|------|----------|
| 单步调用 | 一次 thought 只调一个 Tool | 简单问答、低复杂任务 |
| 串行调用 | 上一个结果决定下一个 Tool | 查订单后查物流、查车型后查库存 |
| 并发调用 | 多个独立 Tool 同时查 | 同时查价格、库存、门店信息 |

建议默认策略是：

- 默认单步
- 有明确依赖时串行
- 完全独立且只读时并发

虽然只读 Tool 可以比写入 Tool 更大胆地并发，但也不要一开始就让模型随意并发调用全部 Tool，否则会导致：

- 成本上升
- 治理复杂
- 异常处理更难

### Tool 设计上的几个取舍

#### 大而全 Tool vs 小而专 Tool

| 方案 | 优点 | 缺点 |
|------|------|------|
| 大而全 Tool | Tool 数量少，模型选择简单 | 参数复杂，歧义多，治理难 |
| 小而专 Tool | 语义清晰，参数简单，治理容易 | Tool 数量增多，选择成本上升 |

建议优先"小而专"，尤其在早期阶段。

#### 直接返回原始文本 vs 返回结构化结果

| 方案 | 优点 | 缺点 |
|------|------|------|
| 原始文本 | 实现快 | 难审计、难复用、难做稳定推理 |
| 结构化结果 | 可治理、可复用、可程序消费 | 设计成本更高 |

建议统一结构化。

#### Tool 内自带鉴权 vs 调度层统一鉴权

| 方案 | 优点 | 缺点 |
|------|------|------|
| Tool 内自带鉴权 | 局部封装强 | 易重复、规则分散 |
| 调度层统一鉴权 | 规则统一、便于审计 | 需要更强基础设施 |

建议：共性鉴权放调度层，特殊业务校验保留在 Tool 内。

#### 只读 Tool vs 可写 Tool

| 方案 | 优点 | 缺点 |
|------|------|------|
| 只读 Tool | 安全边界清晰、易审计、易上线 | 不能直接闭环执行事务 |
| 可写 Tool | 自动化闭环能力强 | 风险高、治理复杂、确认链路重 |

结合你们当前阶段，明显应该选择只读 Tool。

原因不是"能力不够强"，而是：

- 当前目标是先把 Agent 做成可信的查询和辅助决策系统
- 不把执行权交给模型，可以显著降低系统风险
- 先把查询链路、记忆链路、推理链路跑稳，比过早引入写操作更合理

### Tool 在运行时的位置

```text
LLM 生成 thought
   ->
判断需要 action
   ->
选择 tool + arguments
   ->
Hook 做鉴权 / 校验 / 审计
   ->
执行 tool
   ->
获得 observation
   ->
写回 session context
   ->
继续下一轮推理
```

如果展开成更完整的执行链路，建议是：

```text
model outputs tool call
   ->
ToolRegistry resolve tool
   ->
ToolDispatcher validate args
   ->
auth / risk / quota check
   ->
beforeTool hooks
   ->
execute tool
   ->
normalize result
   ->
afterTool hooks
   ->
append observation
   ->
decide continue / retry / ask user / finish
```

### Tool 和记忆系统的边界

要明确区分：

- `Tool` 负责拿"外部事实"
- `Memory` 负责保留"历史上下文"

例如：

- "这个用户刚才提过预算 20 万"是 `Conversation`
- "这个用户历史上偏好德系车"是 `Story`
- "这台车今天库存 5 辆"是 `Tool + KV/DB`
- "这款车的参数说明"是 `Tool + Vector Store`

这条边界很重要。  
如果边界不清，系统很容易把"事实查询"和"记忆召回"混成一层，最终导致上下文污染。

### Tool 机制的最终落点

如果用一句话总结这部分设计，我建议这样定义：

> Tool 机制不是"给模型暴露几个函数"，而是围绕"只读能力定义、可见性控制、参数治理、执行调度、错误处理、结果标准化、风险审计"构成的一整套可执行协议。

只有这样，Tool 才不是一个 demo 级 function list，而是真正能支撑生产级 Agent 的查询能力层。

---

## 十、Agent 评测系统设计

评测是 Agent 质量保障的核心基础设施。没有评测，一切改动都是"我觉得变好了"，而不是"数据显示变好了"。

### 为什么需要评测

Agent 上线后面临四个核心问题：

| 问题 | 描述 |
|------|------|
| **质量不可知** | Agent 回复好不好？没有量化指标 |
| **改了不知道效果** | 改了 Prompt/模型/记忆策略，效果变好了还是变差了？ |
| **模型升级风险** | 换了新模型/新 Provider，会不会出问题？ |
| **回归测试困难** | 新配置/新 Tool 上线，会不会破坏已有场景？ |

评测的价值就是：**量化质量、降低风险、支撑迭代、让每一次改动都有据可依**。

### 评测的核心概念

| 术语 | 解释 |
|------|------|
| **评测目标（EvalTarget）** | 被评测的对象，如 SalesAgent；对外暴露一个标准接口，Foranx/评测平台统一调度 |
| **测试用例（Test Case）** | 一个完整测试场景，包含模拟输入和期望行为 |
| **会话线程（Thread）** | 一次评测对话的容器，关联模型、语言、账户 |
| **消息（Message）** | 对话中的每条消息，包含角色、内容、TraceID、评分 |
| **执行记录（Run）** | 一次 Agent 执行的技术记录：token、耗时、工具调用次数、完成原因 |
| **评分（Score）** | 人工对 Agent 回复的打分（1-5），低分必须附带原因 |
| **EvalGuard** | 评测模式下拦截写操作 Tool 的保护器 |
| **EvalReplayContext** | 回放上下文，注入历史时间戳以保证评测环境逼真 |

### 数据模型关系

```
Thread (会话线程)
├── id
├── name               # 测试用例名称
├── account_id         # 账户
├── model_id           # 使用的模型版本
├── language_code      # 语言
│
├── Message[] (消息列表)
│   ├── id
│   ├── role           # user / assistant / tool
│   ├── content        # 消息内容
│   ├── trace_id       # 日志追踪 ID
│   ├── trace_url      # 跳转日志查询
│   ├── score          # 1-5
│   ├── reasons        # []string 低分原因
│   ├── comment        # 详细反馈
│   └── attachment[]   # 附件
│
└── Run[] (执行记录)
    ├── id
    ├── status         # queued / in_progress / completed / failed
    ├── prompt_tokens
    ├── completion_tokens
    ├── total_tokens
    ├── latency_ms
    ├── tool_call_count
    ├── user_message_count
    ├── assistant_message_count
    └── finish_reason  # stop / length / tool_calls / content_filter / error / handoff
```

### 评测流程：四步闭环

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ 构造用例  │───▶│ 自动执行  │───▶│ 人工打分  │───▶│ 自动分析  │
│  (人工)   │    │  (自动)   │    │  (人工)   │    │  (自动)   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
     │               │               │               │
     ▼               ▼               ▼               ▼
写测试场景       Agent 自动回复    人看回复打分     系统生成报表
(模拟真实客户)   (批量跑)          (1-5 分)        (分布/原因/趋势)
```

#### 第一步：构造测试用例（人工/半自动）

测试用例本质上是**模拟一个真实的用户对话场景**。

设计原则：

- **覆盖典型场景**：产品咨询、事实查询、FAQ 问答
- **覆盖边界情况**：模糊输入、多轮对话、异常中断
- **可复现**：相同输入在相同配置下产生一致的行为
- **有明确期望**：评测人知道什么是"好"的回复

用例来源：

| 来源 | 描述 |
|------|------|
| **人工构造** | 根据业务场景和常见问题设计 |
| **真实数据脱敏** | 从生产环境提取，脱敏后使用 |
| **Bad Case 回流** | 用户投诉/反馈的案例，加入测试集 |
| **边界测试** | 针对特定边界情况（超长输入、特殊字符、多轮追问） |

#### 第二步：自动执行（批量）

系统自动执行，无需人工干预：

```
1. 读取测试用例
2. 标记评测模式（跳过 Memory 写回、跳过真实消息发送）
3. 注入评测回放上下文（历史时间戳）
4. 调用 Agent（走完整 ReActLoop + Tool）
5. Agent 自动处理：意图理解 → Tool 查询 → 推理 → 生成回复
6. 保存结果：Agent 回复、Tool 调用记录、Token 用量、执行耗时、TraceID
```

支持批量执行，一次跑 N 个用例。

#### 第三步：人工打分

评测人在评测平台上逐条打分。

打分规则：

| 分数 | 含义 | 是否需要原因 |
|------|------|-------------|
| 5 | 非常好 | ❌ |
| 4 | 好 | ❌ |
| 3 | 一般 | ✅ **必须填写** |
| 2 | 差 | ✅ **必须填写** |
| 1 | 非常差 | ✅ **必须填写** |

预定义原因枚举：

| 原因 | 描述 |
|------|------|
| `not_helpful` | 没有解决用户问题 |
| `irrelevant` | 与用户问题无关 |
| `incorrect` | 包含错误信息 |
| `too_generic` | 回复太笼统 |
| `other` | 其他问题 |

为什么打分必须是人工的？因为自动打分无法理解业务语义——它不知道"主动追问需求"比"直接甩产品列表"更好，也不知道某个数字是否准确。

#### 第四步：自动分析报告

系统自动生成评测报告：

```
评测报告

总览
──────────────────────────
测试用例数：100
平均分：4.2 / 5
高分率（≥4分）：72%
低分率（≤2分）：8%

分数分布
5分 ████████████████████ 35%
4分 ██████████████████ 37%
3分 ████████████ 20%
2分 ████ 5%
1分 ██ 3%

低分原因分析
过于通用：45%
信息错误：30%
回复无帮助：20%
其他：5%

场景分析
场景          平均分   用例数   状态
产品查询      4.5      30      ✅
FAQ 问答      4.3      25      ✅
复杂推理      3.1      20      ⚠️ 需要优化
边界异常      2.8      15      ❌ 优先优化
```

### 评测可控制的变量

评测的核心方法是**控制变量法**：每次只改一个维度，其他全部一致。

五层可控变量：

```
1. 模型层（Provider）
   - Provider / ModelID
   - 模型参数（temperature、max_tokens）
   - 模型端点（不同环境）

2. System Prompt 层
   - Prompt 内容（角色定义、回复风格、工具使用规则）
   - Prompt 版本（v1 / v2 / v3）
   - Context 分块结构（Conversation / Story / KV / Vector / Tools 的上下游比例）

3. 输入层
   - 当前消息内容
   - 用户画像（Story 数据）
   - 语言
   - 当前 Conversation 状态（摘要、已确认事实、任务状态）

4. Tool 层
   - 可用 Tool 列表（启用/禁用某些 Tool）
   - Tool 参数约束
   - Tool 行为（Mock vs 真实）
   - Tool 分层可见性策略

5. 配置层
   - 记忆策略（Story / Conversation 启用/禁用）
   - ReActLoop 最大步数
   - 上下文 token 配额分配
   - Hook 启用/禁用
```

变量组合示例：

| 评测目的 | 控制的变量 | 固定的变量 |
|---------|-----------|-----------|
| **Prompt 优化** | Prompt 版本 | 模型、输入、Tool |
| **模型对比** | Provider/Model | Prompt、输入、Tool |
| **Tool 效果** | Tool 启用/禁用 | 模型、Prompt、输入 |
| **记忆效果** | Story 启用/禁用 | 模型、Prompt、Tool |
| **长上下文压缩** | 压缩策略参数 | 模型、Prompt、Tool |

### 评测模式的安全机制

评测模式下 Agent 需要照常推理和调用 Tool，但**绝不能对外产生任何副作用**。这套安全机制依赖三个组件协同工作：

#### 评测模式标记（Context Flag）

用 ThreadLocal / RequestContext 传递标记，不做全局变量：

```text
EvalContext.setEvalMode(true)
   →
后续所有组件通过 EvalContext.isEvalMode() 检查
   →
如果是评测模式：
   - 跳过 Story / Conversation 的 Memory 写回
   - 跳过真实消息发送
   - 跳过生产数据库写入
   - 启用 Tool EvalGuard
```

标记在异步线程池中也能正确传递（通过 RequestContextHolder 或 MDC 透传关键 flag）。

#### EvalGuard：写操作 Tool 的保护器

即使当前系统的 Tool 都是只读查询型，框架层面仍然需要 EvalGuard 机制——**它是一种防御性设计，防止未来有人误注册写入型 Tool**。

保护逻辑：

```text
Tool 执行前：
   →
1. 检查是否为评测模式（EvalContext.isEvalMode()）
2. 检查 Tool 是否声明为写操作（IsWriteOperation）
3. 如果评测模式 + 写操作 →
     拦截 → 调用 MockRun 返回模拟结果（不执行真实逻辑）
4. 如果评测模式 + 只读 Tool →
     正常执行（查询类 Tool 无风险）
```

每个 Tool 可以实现 `EvalSafeTool` 接口来声明自己的性质：

- `IsWriteOperation() bool`：声明该 Tool 是否为写操作
- `MockRun(ctx, argumentsInJSON) string`：评测模式下的 mock 实现

这一层的存在让系统在"所有 Tool 都是只读"的前提下，仍然具备防御能力。

#### EvalReplayContext：历史时间回放

Agent 内部可能会用 `time.Now()` 来做时间判断。如果评测用例是一个历史对话，直接用当前时间会导致上下文不一致。

解决方案：从 AgentInput 中提取历史时间，注入 ctx：

```text
buildEvalReplayContext(agentInput)
   → 优先从 MetaInfo.StartTime 提取
   → 次选从最后一条消息的 CreatedAt 提取
   → 注入 ctx
   → 后续所有 time.Now() 替换为历史时间
```

### 核心接口设计

#### EvalTarget 接口

```java
public interface EvalTarget {
    /** 评测目标唯一标识 */
    String id();

    /** 展示名称 */
    String name();

    /** 执行评测 */
    EvalInvokeResult invoke(AgentContext ctx, Map<String, Object> evalInput);
}
```

这个接口是评测系统的核心抽象。每个可评测的 Agent 目标实现此接口，由 EvalRegistry 统一管理，通过评测平台 SPI 对外暴露。

#### EvalRegistry：评测目标注册表

```java
public interface EvalRegistry {
    /** 注册评测目标 */
    void register(EvalTarget target);

    /** 根据 ID 查找 */
    EvalTarget findById(String id);

    /** 列出全部已注册目标 */
    List<EvalTargetInfo> listAll();
}
```

#### Store：评测数据存储接口

```java
public interface EvalStore {
    // Thread 操作
    Thread createThread(CreateThreadRequest req);
    Thread getThread(long threadId);
    void deleteThread(long threadId);

    // Message 操作
    Message createMessage(long threadId, MessageInput input);
    List<Message> listMessages(long threadId);

    // Score 操作
    void saveScore(ScoreInput input);
    Score getScoreByMessageId(long messageId);

    // Run 操作
    Run createRun(long threadId, RunInput input);
    List<Run> listRuns(long threadId);
}
```

### 评测模式下的 ReActLoop 适配

评测模式不会让 ReActLoop 走另一套逻辑，而是通过 Hooks 和 Context Flag 让同一套主干在评测模式下自动跳过写回链路。

在现有的 Hook 生命周期中，增加评测专用的 Hook 行为：

| Hook | 非评测模式 | 评测模式 |
|------|-----------|----------|
| `onConversationUpdated` | 写 Conversation | **跳过（不写）** |
| `onStoryUpdated` | 写 Story | **跳过（不写）** |
| `onBeforeToolCall` | 正常鉴权 | 鉴权 + **检查是否被 EvalGuard 拦截** |
| `onSessionEnd` | 写 Session 摘要 | **仅记录 trace，不写回** |

所以评测模式下 Agent 的链路变成：

```
构造 AgentInput（模拟用户 + 历史上下文）
   →
标记 EvalContext.setEvalMode(true)
   →
注入 EvalReplayContext（历史时间）
   →
组装 Context（正常流程，但不捞 Story/Conversation 的生产数据）
   →
进入 ReActLoop（正常推理）
   →
Tool 调用 → EvalGuard 检查 → 正常执行（只读 Tool）
   →
出 ReActLoop → Hooks 检测到评测模式 → 跳过 Memory 写回
   →
只保存 Run 记录 + Message + TokenUsage 到评测库
```

### 评测与 Hook 的交互

评测触发时，需要增加一个专用 Hook：`onEvalTargetInvoked`，负责：

- 记录评测执行的 trace
- 收集 Token 用量
- 记录 ReActLoop 轮次
- 记录 Tool 调用明细
- 生成 TraceURL 方便后续排查

这不属于日常 Hook 生命周期，而是在 `EvalTarget.invoke()` 内部插入的一个观测点。

### 评测代码的目录组织建议

```
├── eval/                    # 评测模块
│   ├── model/               # 数据模型：Thread、Message、Run、Score
│   │   ├── Thread.java
│   │   ├── Message.java
│   │   ├── Run.java
│   │   └── Score.java
│   │
│   ├── evaluation/          # 评测核心：EvalTarget 接口 + Registry
│   │   ├── EvalTarget.java
│   │   ├── EvalRegistry.java
│   │   └── AgentEvalTarget.java   # Agent 的评测目标实现
│   │
│   ├── service/             # 业务服务：Thread、Message、Score CRUD
│   │   ├── ThreadService.java
│   │   ├── MessageService.java
│   │   └── ScoreService.java
│   │
│   ├── store/               # 存储接口 + 实现
│   │   ├── EvalStore.java
│   │   └── MysqlEvalStore.java
│   │
│   └── guard/               # 评测安全
│       ├── EvalGuard.java          # Tool 拦截判断 + Mock
│       └── EvalReplayContext.java  # 历史时间回放
```

### 评测治理的几个规范

1. **低分必须有原因**。分数 ≤ 3 时必须至少选一个原因。防止"随手打个低分但没有说明"导致问题无法定位。

2. **只允许对 Agent 回复评分**。用户消息（模拟输入）不能被评分，只有 Agent 产生的 Outbound 消息可以被打分。

3. **评测数据和生产隔离**。评测产生的 Thread / Message / Run / Score 全部进评测库，不进生产库。Story 和 Conversation 在评测模式下**不写回**。

4. **TraceID 必须贯穿**。每个评测 Run 都有 TraceID，从执行链路到结果存储全程可追踪。评测平台的 UI 上可以一键跳转到 Trace 页面。

5. **批量执行 + 分工打分**。一次批量跑 50+ 个用例，多人分工打分。关键用例可以交叉验证（多人打分取平均）。

### 评测闭环：从问题到优化

```
评测报告 → 找到低分用例
   →
查 TraceURL → 分析执行过程：
   - 意图理解是否准确？
   - Tool 调用是否正确？
   - Context 组装是否丢信息？
   - 回复是否过长/过短/不相关？
   →
定位根因 → 针对性优化（Prompt / 记忆策略 / Tool 定义）
   →
重新跑评测 → 对比新旧分数 → 确认效果
```

这就是**持续改进闭环**：构造用例 → 执行 → 打分 → 分析 → 优化 → 再跑。

### 评测对 Context 组装策略的验证作用

评测不只是"看 Agent 答得好不好"，它还能直接验证我们的设计假设。

例如我们说了"Context 按 Conversation → Story → KV → Vector 的顺序装"，靠评测就能验证：

- 把顺序反过来跑一批 → 看分数有没有下降
- Story 全关掉跑一批 → 看分数有没有下降
- Vector Store 的 recall top-k 设为 3 vs 10 → 看分数和 Token 的 trade-off

不做评测就没有这个能力。评测让你可以**拿数据而不是拿直觉**来决策。

### 一句话总结

评测系统是 Agent 质量保障的安全网：

- **EvalTarget** 让 Agent 成为标准化的可评测目标
- **EvalRegistry** 让评测目标可被发现和调度
- **Context Flag** 让评测模式和生产模式共用同一套 ReActLoop
- **EvalGuard** 让系统在评测模式下天然安全
- **Store** 让评测数据可留存、可对比、可分析
- **控制变量法** 让每一轮优化都有数据支撑

---

## 十一、一次完整运行时序

下面是一条完整链路，展示 Scheduler、RequestPreparer、AgentState、三层记忆、ReActLoop、Hooks、Tools 是怎么协作的。

```text
Inbound（商家消息）/ OutReach（定时任务）
    →
Planner.PlanInbound / PlanOutreach
  → 创建 ClientTaskJob（带 dueAt、租约、状态）
  → Inbound 时额外 CancelOtherJobsOnInbound（天然串行化）
  → 异步 Lua 脚本原子写入 Redis：
      ZADD job:due:queue {dueAt} {jobId}
      SETEX job:status:{jobId} {ttl} "PENDING"
    →
Dispatcher.DispatchDue / DispatchDueByTaskNames
  → ZRANGEBYSCORE job:due:queue 0 {now()} LIMIT 100（轮询 Redis，不碰 MySQL）
  → for each jobId:
      GET job:status:{jobId}
      ├── "PENDING" → SET NX EX 30 job:lease:{jobId} {instanceId}（抢租约）
      │   └── 成功 → 异步交给 Engine.ProcessTask
      └── "CANCELLED"/"DONE" → ZREM job:due:queue {jobId}（清理脏数据）
  → （Redis 不可用时降级到 MySQL 轮询）
    →
Engine.ProcessTask
  → 构建 TaskPayload（ClientProfile + ActiveAsset + ChannelBinding）
  → 准备会话上下文（创建 Thread + Run，获取 modelVersionID + languageCode）
    →
RequestPreparer.prepare
  → 解析运行环境：userId、taskType、languageCode、channelId
  → 产出干净的 AgentRequest（不查业务数据）
    →
Agent.handle(AgentRequest)
  → 初始化 AgentState
     → InputState.InitFromAgentInput（冻结输入）
     → RuntimeState.InitFromAgentInput（预加载附件）
  → 定位 Memory
     → Story(userId)：查旁路缓存 → 未命中则查 MySQL → 写回缓存
     → Conversation(convId)：查旁路缓存 → 未命中则查 MySQL → 写回缓存
     → 检测 Redis 有 checkpoint → 恢复 RuntimeState
  → Context 组装
     → Conversation（本轮摘要 + 状态）
     → Story（画像 + 历史摘要）
     → KV/DB（结构化业务事实）
     → Vector Store（语义知识）
     → Available Tools（经 L2-L4 裁剪）
  → Hooks 前置：onSessionStart / onContextAssembling / onMemoryRetrieved
  → 进入 ReActLoop
     loop:
       1. ModelProvider.chat(request)  → 拿到 thought
       2. 判断：final answer? → break
       3. 选择 Tool + 参数
       4. Hooks: onBeforeToolCall（鉴权/校验）
       5. ToolDispatcher 执行 Tool
       6. Hooks: onAfterToolCall（审计/记录）
       7. 标准化 ToolResult → 注入 observation
       8. 追加到 RuntimeState.RuntimeMessages
       9. 每 3-5 轮：Redis SET checkpoint（崩溃恢复用）
       10. 继续下一轮
  → 出 ReActLoop
  → Hooks 后置：onBeforeSessionSummarize
  → 提炼 Session 摘要（结构化字段头 + 自然语言详情）
  → 写回 Conversation（含 sessionType 标记，version 乐观锁）
  → 条件触发 Story 更新（仅稳定的偏好/画像沉淀）
  → 失效旁路缓存：DEL conv:${id}:cache
  → 生成 AgentResponse（finalReply + toolCalls + tokenUsage）
  → 返回
```

---

## 十二、数据写入与压缩策略

Agent 的记忆不是无限的。随着 Conversation 变长、Story 积累，Agent 的每一次推理持有的上下文会持续膨胀——如果不加控制，最终会导致 token 成本失控、推理质量下降。本节描述 Agent 如何管理记忆的写入节奏和体积。

### 推荐的数据写入策略

为了避免记忆膨胀，建议明确区分同步写入和异步写入。

#### 同步写入

这些内容建议在请求主链路内完成：

- 创建 `Session`
- 更新 `Conversation` 的当前摘要和状态
- 记录关键工具结果引用

因为这些数据会直接影响同一 `Conversation` 内下一轮请求。

#### 异步写入

这些内容可以异步化：

- `Conversation -> Story` 的长期沉淀
- 各类埋点、观测、trace
- 非关键日志
- 向量化索引或离线分析任务

这样既保证对话连续性，又不会让主链路过重。

### 压缩与裁剪策略

随着 `Conversation` 变长，Context 一定会膨胀，所以必须控制。

#### Conversation 侧压缩

建议只保留两部分：

- 最近若干轮高保真摘要
- 更早内容的更粗粒度摘要

不要把所有历史 Session 原样塞回去。

#### Story 侧裁剪

`Story` 中只保留稳定、高价值、跨轮有效的信息，例如：

- 长期偏好
- 稳定标签
- 历史重要事件

不稳定、一次性的内容不要进 `Story`。

#### Retrieval 侧限流

无论是 `KV/DB` 还是 `Vector Store`，都要限制每次召回数量。

建议做法：

- 每个来源单独设 token budget
- 同类内容去重
- 只保留 top-k
- 对结果做短摘要后再入 Context

---

## 十三、Agent 实现路线图

如果你要基于这份文档从头搭建这个 Agent，推荐按下面的顺序构建——每一步产出可运行的中间版本，而不是等全部设计好再一起写代码。

### 第一阶段：Agent 骨架

**产出**：一个能接收请求、调模型、返回结果的极简 Agent。

| 步骤 | 做什么 | 验证方式 |
|------|--------|----------|
| 1.1 | 定义 `Agent` 接口、`AgentRequest`、`AgentResponse`、`AgentConfig` | 编译通过 |
| 1.2 | 实现最简单的 ReActLoop（不调 Tool，只调模型、接收输出、直接返回） | 输入 "你好"，Agent 能回复 |
| 1.3 | 实现模型调用的抽象层（当前只接一个 Provider） | 换模型不需要改 Agent 代码 |

### 第二阶段：只读 Tool 能力

**产出**：Agent 能通过 Tool 查询外部事实。

| 步骤 | 做什么 | 验证方式 |
|------|--------|----------|
| 2.1 | 定义 `Tool` 接口、`ToolResult`、`ToolExecutionContext` | 编译通过 |
| 2.2 | 实现 `ToolRegistry`（L1 Base Registry，平铺注册） | 注册 3 个 mock Tool，Agent 能选中并调用 |
| 2.3 | 实现 `ToolDispatcher`（参数校验、超时、错误标准化、重试） | Tool 超时/失败时 Agent 不崩溃 |
| 2.4 | 打通 ReActLoop 的 Tool 调用循环 | Agent 自动判断"先查 Tool 再回答" |

### 第三阶段：三层记忆

**产出**：Agent 能记住本轮对话和跨轮用户画像。

| 步骤 | 做什么 | 验证方式 |
|------|--------|----------|
| 3.1 | 定义 `Session`、`Conversation`、`Story` 实体和存储接口 | 编译通过 |
| 3.2 | 实现 Context 组装：开始前捞 Conversation + Story，拼进 prompt | 问"我刚才说了什么"，Agent 能引用上文 |
| 3.3 | 实现 Session 摘要写回 Conversation | 多轮对话后 Agent 不丢失上下文 |
| 3.4 | 实现 Conversation 摘要条件沉淀到 Story | 新 Conversation 中 Agent 能引用历史偏好 |

### 第四阶段：Hooks 治理

**产出**：Agent 的每一步都有 Hook 扩展点。

| 步骤 | 做什么 | 验证方式 |
|------|--------|----------|
| 4.1 | 定义 Hook 接口和生命周期 | 编译通过 |
| 4.2 | 挂上 `onBeforeToolCall`（鉴权）、`onSessionEnd`（trace 记录） | Hook 抛异常不影响 Agent 主流程 |
| 4.3 | 挂上观测 Hook：记录 token 用量、耗时、Tool 调用明细 | 每次请求后可查用量和耗时 |

### 第五阶段：Tool 分层注册

**产出**：Tool 按域管理、按权限裁剪、按 Session 收敛。

| 步骤 | 做什么 | 验证方式 |
|------|--------|----------|
| 5.1 | 实现 L2 Domain Registry（按业务域编组） | 销售场景和售后场景看到不同 Tool 集 |
| 5.2 | 实现 L3 Policy Filter（按角色/租户/环境裁剪） | 未登录用户看不到订单 Tool |
| 5.3 | 实现 L4 Session Visible Set（按问题和 token budget 收敛） | 问物流时不暴露门店 Tool |

### 第六阶段：评测体系

**产出**：每改一个配置都能跑评测看效果。

| 步骤 | 做什么 | 验证方式 |
|------|--------|----------|
| 6.1 | 实现 `EvalTarget` 接口、`EvalContext` 标记 | 评测模式下 Agent 不写回 Memory |
| 6.2 | 实现 `EvalGuard`（防御性写操作拦截） | 评测模式下写 Tool 被 Mock |
| 6.3 | 构造 20 个测试用例，实现批量执行 + 人工打分 | 拿到第一份评测报告 |
| 6.4 | 用控制变量法验证一次 Prompt 改动 | 改 Prompt 后分数变化可量化 |

### 关键提醒

- **第一阶段就可以跑通**：不要等到六个阶段全做完才验证。骨架 + 单 Tool = 就是可用的最小 Agent
- **评测不要放在最后**：第三阶段做完记忆就应该开始写评测用例。没有数据佐证，你不知道记忆策略改得好不好
- **Hook 是插件**：不要在第一阶段就把所有 Hook 挂上。先把主链路跑稳，再逐步加治理能力

---

## 十四、实现映射建议

结合当前 YHarness 的结构，可以这样映射：

| 能力 | 建议落点 |
|------|----------|
| `ReActLoop` | `core/` |
| `AgentContext` 组装 | `context/` |
| `Conversation` / `Story` / `Session` 持久化 | `context/` 或新增 `memory/` 子包 |
| `Hook` 生命周期扩展 | `hooks/` |
| `Tool` 注册与调度 | `tools/` |
| `KV/DB` / `Vector Store` 访问封装 | `tools/` 或 `service/` |

这里即使增加一个 `memory/` package，也只是代码组织层面的目录，不意味着新增业务对象抽象。  
业务概念层面仍然只有 `Session / Conversation / Story` 三层。

---

## 十五、最终结论

这个 Agent 系统设计的完整蓝图就是：

> **一个基于 ReActLoop 执行引擎的只读查询型 Agent，通过 TaskScheduler（Planner/Dispatcher/Engine）做任务编排和串行化，通过 RequestPreparer 做请求环境解析，通过 AgentState 做运行时状态承载，通过三层记忆（Session → Conversation → Story）保持上下文连续性，通过 Hooks 做生命周期治理，通过只读 Tool 查询外部事实，通过评测体系量化每一次变更的效果。它不引入额外业务抽象对象，仅凭上述八个子系统协作完成全流程。**

### Agent 的八个子系统回顾

| 子系统 | 一句话 |
|--------|--------|
| **TaskScheduler** | Agent 的入口——Planner 创建/取消 Job，Dispatcher 锁竞争后分发 |
| **RequestPreparer** | Agent 的翻译层——将原始任务解析为干净的 AgentRequest |
| **AgentState** | Agent 的运行容器——InputState + RuntimeState + OutputState + PerfState |
| **ReActLoop** | Agent 怎么跑——推理-行动-观察的闭环 |
| **Memory（三层）** | Agent 记什么——AgentState 承载运行、Conversation 保持本轮连续、Story 沉淀跨轮画像、Session 持久化记录 |
| **Context 组装** | Agent 用记忆的方式——按 Conversation → Story → KV/DB → Vector 的优先级拼装 |
| **Hooks** | Agent 的治理——观测、注入、审计、安全，不污染主流程 |
| **Tools（只读）** | Agent 的眼睛——查询外部事实，绝不写入 |
| **Eval 评测** | Agent 的体检——控制变量法量化质量 |

### 关键设计决策速览

| 决策 | 选择 | 理由 |
|------|------|------|
| 运行时状态容器 | AgentState（非 Session） | Session 需持久化，AgentState 含运行时资源，职责分离 |
| 存储方案 | 旁路缓存 + Redis Checkpoint + version 乐观锁 | 高频读 Redis 缓存，崩溃恢复靠 checkpoint，写冲突靠乐观锁 |
| 调度队列设计 | Redis Sorted Set 调度前端（v2），MySQL 纯持久化 + Lua 脚本同步 + 三层一致性兜底 | 剥离调度职责优化 MySQL "热表"问题；三层兜底（原子写入 + Engine 双检查 + 定时重建）保证最终一致 |
| Story 结构 | 混合结构 C（核心结构化 + 扩展 Map） | 核心字段可索引，扩展字段可自演化 |
| 记忆主键 | Story = userId，Conversation = (channelAccountId, userId, conversationId) | 统一画像+按来源标记 vs 对话完全隔离 |
| Tool 注册 | 两段式（元数据与执行体分离） | 审计不启动执行体，裁剪不加载执行体，热上新只替换执行体 |
| ModelProvider | 统一抽象 A（适配器模式） | ReActLoop 与模型解耦，切换模型改一行配置 |
| Context Token 分配 | 全塞 + 两端式排序 + Conversation 双层摘要（策略 C 增强） | 不做每轮动态判断，性能最优；两端式缓解 lost-in-the-middle；双层摘要控制中间区膨胀 |
| Context 两端式 | 开头=输入+工具，结尾=Tool结果+推理态，中间=历史 | 模型注意力两端高，重要信息放两端 |
| 多语言 | 混合 C（System Prompt 约束 + 术语表注入 + 语言检测 Hook） | 平衡质量与成本 |
| OutReach 文案 | 混合 C（首条模板约束，后续自由发挥） | 首屏品牌可控 + 深入对话灵活 |
| 触达策略注入 | C（RequestPreparer 预注入 + Agent 必要时 Tool 重查） | 默认零延迟，必要时自己查 |
| 渠道感知 | Agent 可见 channelAccountId | channelAccountId + userId 共同确定记忆归属 |
| Vector Store 召回 | 两级召回：score>0.7 入正文，0.4~0.7 入附录 | 不需要每轮动态预算，自然控制召回量 |
| Planner 决策 | 纯规则引擎（非 AI） | 确定性强、可审计、易调试 |
| Session 摘要 | 纯文本 + 关键字段标记 + sessionType | 关键信息可程序化提取，详情可自然表达 |
| Eval 模式 | Context Flag + EvalGuard 双重安全锁 | 评测不污染主记忆链路 |
| 入口串行化 | Planner.CancelOtherJobsOnInbound | 天然防止同一商家并发穿插 |

### 从文档到代码

从 [十三、Agent 实现路线图](#十三agent-实现路线图) 开始，按六个阶段逐步构建——第一阶段即可得到一个可用的极简 Agent，逐步叠加记忆、治理、评测能力。

这个 Agent 不是一个实验性玩具——它定义了清晰的接口、明确的子系统边界、可验证的每一步。**拿着这份文档，一个工程师可以从零开始写出完整的生产级 Agent。**