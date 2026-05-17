# YHarness - AI Agent 框架 Harness 实践

> 一个**零依赖 AI 框架**的 SpringBoot 实现，手工完成 API 对接与 Harness 工程细节，自定义程度极高。

一个基于 SpringBoot 的 AI Agent 框架 Harness 实现，支持 ReAct 循环、Context 管理和多 AI Provider 集成。

> 📌 **场景示例**：本项目以股票咨询场景作为示例展示 Harness 的完整实现。使用者可以参考此范式，将其改造为客服、教育、医疗等任何领域的 Agent 系统。

##  核心优势

| 特性 | 传统 AI 框架 | YHarness |
|------|-------------|----------|
| 依赖 | 依赖重型 AI 框架 | ✅ 零额外 AI 框架依赖 |
| 定制 | 框架封装，定制受限 | ✅ 纯手工 API 对接，完全可控 |
| 体积 | 框架庞大，包体积大 | ✅ 轻量级，仅依赖核心库 |
| 学习 | 学习框架特定概念 | ✅ 学习通用 AI 调用原理 |

### 技术选型哲学

- **不使用** LangChain、LangFlow、Semantic Kernel、Dify 等 AI 框架
- **不使用** Spring AI、Spring豆荚 等集成框架
- **仅使用**：SpringBoot（Web）+ OkHttp（HTTP）+ Jackson（JSON）
- **深度理解**：从零理解 AI Agent 的 ReAct 循环、Context 管理、工具调用原理

##  项目介绍

YHarness 是一个 **Harness（框架脚手架）**，旨在帮助开发者从零构建 AI Agent 应用。核心功能包括：

- 完整的 ReAct（Reason + Action）循环实现
- 灵活的 Context 上下文管理机制
- 可扩展的工具调用系统
- 多 AI Provider 支持（OpenAI、Anthropic）
- 参考 ClaudeCode 的 Hooks 生命周期扩展

##  技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 1.8 |
| 框架 | SpringBoot | 2.7.x |
| HTTP客户端 | OkHttp | 4.12.x |
| JSON处理 | Jackson | 2.15.x |
| 日志 | SLF4J + Logback | - |

**仅此 4 个核心依赖**，无任何 AI 框架！

##  快速开始

###  环境要求

- JDK 1.8+
- Maven 3.6+

###  配置

1. 复制配置文件模板：
   ```bash
   cp src/main/resources/application-dev.yml src/main/resources/application.yml
   ```

2. 编辑 `application.yml`，填入你的 API Key：
   ```yaml
   yharness:
     provider:
       api-key: your-actual-api-key-here
   ```

###  构建与运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 或打包后运行
mvn clean package -DskipTests
java -jar target/yharness-1.0.0.jar
```

###  API 测试

```bash
# 运行测试脚本
python3 test_agent_api.py
```

##  项目结构

```
YHarness/
├── src/main/java/com/yancy/yharness/
│   ├── core/                    # ReAct 核心引擎（纯手工实现）
│   ├── context/                 # 上下文管理
│   ├── provider/               # AI Provider 实现（直连 API）
│   ├── hooks/                  # Hooks 机制
│   ├── tools/                  # 工具模块（示例：股票咨询工具）
│   ├── config/                 # 配置类
│   └── controller/              # REST 控制器
├── src/main/resources/
│   ├── application.yml         # 应用配置（本地，不提交）
│   └── application-dev.yml     # 配置模板
├── test_agent_api.py           # API 测试脚本
├── TECHNICAL_DESIGN.md         # 技术设计文档
└── README.md                   # 项目文档
```

##  核心模块

###  ReAct 循环

实现完整的 Reason + Action 循环：

1. 用户输入 → 2. 构建 Prompt → 3. 调用 AI
4. 解析响应 → 5a. 工具调用 / 5b. 直接回答
6. 执行工具/返回答案 → 7. 记录到 Context

**纯手工实现**，不依赖任何 AI 框架的 ReAct 组件。

核心代码位置：[ReActEngine.java](src/main/java/com/yancy/yharness/core/ReActEngine.java)

###  Context 管理

包含丰富的上下文信息：

- 系统提示词（System Prompt）
- 消息历史（Message List）
- 工具定义（Tool Definitions）
- 长期记忆（Long-term Memory）
- 业务状态（Business State）

核心代码位置：[AgentContext.java](src/main/java/com/yancy/yharness/context/AgentContext.java)

###  Hooks 机制

参考 ClaudeCode 的 14 个 hooks 设计，支持生命周期扩展：

| Hook | 时机 |
|------|------|
| `onAgentStart` | Agent 启动时 |
| `onAgentEnd` | Agent 结束时 |
| `onLoopStart` | 循环开始时 |
| `onLoopEnd` | 循环结束时 |
| `beforeToolCall` | 工具调用前 |
| `afterToolCall` | 工具调用后 |
| ... | ... |

核心代码位置：[HookManager.java](src/main/java/com/yancy/yharness/hooks/HookManager.java)

###  Provider 支持

| Provider | 说明 |
|----------|------|
| OpenAI Provider | 支持 OpenAI 及兼容 API（DeepSeek 等） |
| Anthropic Provider | 支持 Claude 等 |

**直连 API**，手工处理请求/响应，不依赖框架封装。

核心代码位置：[OpenAIProvider.java](src/main/java/com/yancy/yharness/provider/OpenAIProvider.java)

###  工具系统

工具系统设计为可扩展，只需实现 `Tool` 接口即可添加新工具：

```java
public interface Tool {
    String getName();                    // 工具名称
    String getDescription();            // 工具描述（供 AI 理解）
    ToolDefinition getDefinition();     // 工具参数定义
    String execute(AgentContext context, Map<String, Object> arguments);  // 执行逻辑
}
```

> 📌 **场景扩展**：本项目的工具示例为股票咨询场景（行情查询、风险评估等）。替换为客服、教育等场景时，只需实现对应的工具类即可。

##  场景替换指南

将股票咨询场景替换为其他场景的步骤：

1. **修改系统提示词** - 编辑 `application.yml` 中的 `system-prompt`
2. **替换工具实现** - 修改 `tools/` 目录下的工具类
3. **调整业务状态** - 修改 `AgentContext` 中的业务状态结构
4. **更新测试用例** - 修改 `test_agent_api.py` 中的测试消息

##  API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/agent/health` | GET | 健康检查 |
| `/api/agent/chat` | POST | 发送消息 |
| `/api/agent/context/{id}` | GET | 获取上下文 |
| `/api/agent/context/{id}` | DELETE | 清除上下文 |

##  开源项目

###  许可证

本项目采用 MIT 许可证。

###  贡献指南

欢迎提交 Issue 和 Pull Request！

####  提交 Issue

- 提交 Bug 请描述清楚问题、复现步骤、环境信息
- 提交 Feature 请描述清楚需求场景、功能预期

####  提交代码

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

####  代码规范

- 遵循 Google Java Style Guide
- 提交前确保通过 `mvn compile`
- 新功能请添加适当的注释和文档
- 测试覆盖新增功能

####  Commit 消息规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

Type 类型：
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

##  联系方式

- 项目 Issues: https://github.com/yancy/YHarness/issues

##  致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [OkHttp](https://square.github.io/okhttp/)
- [Jackson](https://github.com/FasterXML/jackson)
