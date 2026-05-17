# YHarness - 股票咨询Agent框架

一个基于 SpringBoot 的 AI Agent 框架，专注于股票投资咨询场景，支持 ReAct 循环、Context 管理和多 AI Provider 集成。

##  项目介绍

YHarness 是一个智能股票投资咨询 Agent 框架，通过 ReAct（Reason + Action）循环机制，结合丰富的工具集，为投资者提供：

- 股票行情查询
- 技术分析与基本面分析
- 投资者风险评估
- 市场资讯获取
- 个性化投资建议

##  技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 1.8 |
| 框架 | SpringBoot | 2.7.x |
| HTTP客户端 | OkHttp | 4.12.x |
| JSON处理 | Jackson | 2.15.x |
| 日志 | SLF4J + Logback | - |

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
│   ├── core/                    # ReAct 核心引擎
│   ├── context/                 # 上下文管理
│   ├── provider/               # AI Provider 实现
│   ├── hooks/                  # Hooks 机制
│   ├── tools/                  # 工具模块
│   │   └── stock/              # 股票咨询工具
│   ├── config/                 # 配置类
│   └── controller/              # REST 控制器
├── src/main/resources/
│   ├── application.yml         # 应用配置（本地，不提交）
│   └── application-dev.yml     # 配置模板
├── test_agent_api.py           # API 测试脚本
├── TECHNICAL_DESIGN.md         # 技术设计文档
└── README.md                   # 项目文档
```

##  核心功能

###  ReAct 循环

实现完整的 Reason + Action 循环：

1. 用户输入 → 2. 构建 Prompt → 3. 调用 AI
4. 解析响应 → 5a. 工具调用 / 5b. 直接回答
6. 执行工具/返回答案 → 7. 记录到 Context

###  Context 管理

包含丰富的上下文信息：

- 系统提示词
- 消息历史
- 工具定义
- 长期记忆
- 投资状态（阶段、持仓、交易记录等）

###  Hooks 机制

参考 ClaudeCode 的 14 个 hooks 设计，支持生命周期扩展。

###  Provider 支持

- OpenAI Provider（支持 DeepSeek 等 OpenAI 兼容 API）
- Anthropic Provider（支持 Claude 等）

##  股票咨询工具

| 工具名 | 功能 |
|--------|------|
| `getStockQuote` | 获取股票实时行情 |
| `analyzeStock` | 股票技术/基本面分析 |
| `assessInvestorRisk` | 投资者风险评估 |
| `getMarketNews` | 获取市场资讯 |
| `generateInvestmentAdvice` | 生成投资建议 |

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
