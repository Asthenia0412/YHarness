# 异常分析报告

## 基本信息
- **分析时间**：2026-05-24 00:02
- **问题简述**：Chat API 绕过调度层直接调用 Agent，破坏了"分派→预处理→执行"的三层架构
- **影响范围**：Agent 系统整体架构，所有请求入口（Chat API / 调度任务 / 评测平台）
- **严重程度**：高

## 问题现象
用户在回顾架构设计时指出：

> "目前的架构设计，并没有按我们最开始预期的一样先做分派、再做pre、再做agent的实际调用。目前的情况是：chat api直接调用了agent的能力。"

代码层面表现为：
```
AgentController.chat()
  → 手动构建 AgentRequest（无标准流程）
  → 直接调用 agent.handle(request)  ← 跳过调度层
```

对比设计文档（design.md）中明确的架构：
```
调度层（Dispatch）→ 准备层（Pre-process）→ 执行层（Agent.handle）
```

## 环境信息
- **项目**：YHarness Agent Framework
- **语言**：Java 1.8
- **框架**：Spring Boot 2.7.18
- **构建工具**：Maven
- **关键依赖**：Lombok 1.18.46, Jackson 2.15.2

## 排查过程

### 1. 问题确认
通过代码审查发现三个直接调用 `agent.handle()` 的入口：

| 入口 | 文件 | 问题 |
|------|------|------|
| Chat API | AgentController.java:42 | 手动构建 AgentRequest，直接调用 agent.handle() |
| 调度任务 | Engine.java:49 | 虽经 RequestPreparer，但仍直接调用 agent.handle() |
| 评测服务 | EvalService.java:102 | 评测模式下直接调用 agent.handle() |
| 评测目标 | AgentEvalTarget.java:58 | 评测目标 SPI 直接调用 agent.handle() |

### 2. 根因分析
根本原因是**缺少统一的请求分派层**（Dispatch Pipeline）：
- 没有标准化的入口点来统一处理 eval 模式检测、上下文注入、隔离检查
- 各个调用方各自构建 AgentRequest，逻辑分散
- 评测模式的 EvalContext 管理分散在各调用方，容易遗漏
- 新增一个请求入口时，开发者需要自己搞清楚"该怎么做"，而不是"直接调 pipeline"

### 3. 验证假设
假设：创建一个统一的 `DispatchPipeline`，所有请求入口都走它，就能解决架构问题。

验证方法：重构四个调用方，全部改为通过 `DispatchPipeline`，编译验证通过。

## 解决方案

### 创建 DispatchPipeline（调度管道）
`pipeline/DispatchPipeline.java` — 统一的请求分派入口，三段式处理：

```
Phase 1: Dispatch（分派阶段）
  ├── 检测请求模式（production / eval）
  ├── 设置 EvalContext（评测模式下 ThreadLocal 隔离）
  
Phase 2: Pre-Process（预处理阶段）
  ├── 填充缺省字段（taskType, languageCode, channelId 等）
  ├── 注入 eval 标记到 metadata
  └── 调用 RequestPreparer 标准化请求

Phase 3: Execute（执行阶段）
  └── Agent.handle(preparedRequest)
  
Phase 4: Cleanup（清理阶段）
  └── 清理 EvalContext（finally 块保证释放）
```

### Pipeline 提供的统一入口方法

| 方法 | 用途 | 内部流程 |
|------|------|----------|
| `dispatchChat()` | Chat API 调用 | Production 模式 → RequestPreparer → Agent |
| `dispatchJob()` | 调度任务执行 | Production 模式 → RequestPreparer → Agent |
| `dispatchEval()` | 评测任务执行 | Eval 模式 → RequestPreparer → Agent |
| `dispatchDirect()` | 直接传入 AgentRequest | Production 模式 → 填充默认值 → Agent |

### 重构的文件

| 文件 | 变更内容 |
|------|----------|
| `AgentController.java` | 注入 DispatchPipeline，`chat()` 调用 `dispatchChat()` |
| `Engine.java` | 注入 DispatchPipeline，`process()` 调用 `dispatchJob()` |
| `EvalService.java` | 注入 DispatchPipeline，`executeSingleTrial()` 调用 `dispatchEval()` |
| `AgentEvalTarget.java` | 注入 DispatchPipeline，`invoke()` 调用 `dispatchDirect()` |

### 修复的预存编译错误

| 文件 | 错误 | 修复 |
|------|------|------|
| `MockEvalPlatformService.java` | Java 8 不支持 switch 表达式 | 改为 if-else 链 |
| `PassAtK.java` | 调用了不存在的 `setPassAtK()` | 改为按 k 值设置 `passAt3`/`passAt5` |
| `EvalController.java:63` | Java 8 不支持 `var` 关键字 | 改用显式 `List<EvalTrial>` 类型 |
| `EvalController.java:104` | Java 8 不支持 `var` 关键字 | 改用显式 `List<EvalTask>` 类型 |

## 验证结果

- `mvn clean compile` — **BUILD SUCCESS**
- 93 个源文件全部编译通过
- 所有直接 `agent.handle()` 调用已收敛到 `DispatchPipeline` 内部

## 经验总结

- **关键知识点**：Agent 系统的三层架构（Dispatch → Pre-process → Execute）不是可选的，而是必须的。缺少任何一层都会导致职责分散、逻辑重复。
- **排查技巧**：搜索 `agent.handle()` 可以快速发现所有绕过调度层的直接调用。
- **预防建议**：
  1. 新请求入口必须通过 `DispatchPipeline`，禁止直接注入 `Agent`
  2. 后续考虑将 `DispatchPipeline` 设计为基础设施接口，所有入口（包括未来新增的）强制要求经过 Pipeline

## 相关资源
- [DispatchPipeline.java](file:///Users/yancy/Desktop/Projects/YHarness/src/main/java/com/yancy/yharness/pipeline/DispatchPipeline.java)
- [AgentController.java](file:///Users/yancy/Desktop/Projects/YHarness/src/main/java/com/yancy/yharness/controller/AgentController.java)
- [Engine.java](file:///Users/yancy/Desktop/Projects/YHarness/src/main/java/com/yancy/yharness/scheduler/Engine.java)
- [设计文档 design.md](file:///Users/yancy/Desktop/Projects/YHarness/docs/design.md) — 第 49~97 行（系统架构总览）