# YHarness

A lightweight AI Agent framework built with pure Java, implementing the ReAct (Reasoning + Acting) paradigm from scratch.

## Overview

YHarness is a SpringBoot-based implementation of an AI Agent Harness, providing:

- Complete ReAct loop implementation
- Flexible context management with automatic compression
- Extensible tool calling system
- Multi-provider support (OpenAI, Anthropic)
- Observer-pattern based Hook system

**Philosophy**: Zero dependency on AI frameworks. Only SpringBoot + OkHttp + Jackson.

## Quick Start

### Prerequisites

- JDK 1.8+
- Maven 3.6+

### Configuration

1. Copy the configuration template:
```bash
cp src/main/resources/application-dev.yml src/main/resources/application.yml
```

2. Update `application.yml` with your API key:
```yaml
yharness:
  provider:
    api-key: your-actual-api-key-here
```

### Build & Run

```bash
# Compile
mvn clean compile

# Run
mvn spring-boot:run

# Package
mvn clean package -DskipTests
java -jar target/yharness-1.0.0.jar
```

### Test

```bash
python3 test_agent_api.py
```

## Project Structure

```
YHarness/
├── src/main/java/com/yancy/yharness/
│   ├── core/                    # ReAct engine implementation
│   ├── context/                # Context management & compression
│   ├── provider/               # AI provider implementations
│   ├── hooks/                  # Hook system
│   ├── tools/                  # Tool implementations (stock scenario)
│   ├── config/                 # Configuration classes
│   └── controller/             # REST controllers
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
└── README.md
```

## Core Concepts

### ReAct Loop

The ReAct (Reasoning + Acting) loop enables AI agents to dynamically plan and execute tasks:

```
while (iterations < max && !isFinal):
    |
    +-- 1. Thought
    |       Call LLM to generate Thought
    |       Determine: tool call or final answer
    |
    +-- 2. Action
    |       Execute tool call OR return final answer
    |
    +-- 3. Observation
            Tool result added to context
            Visible to LLM in next iteration
```

**Exit Conditions**:
- `thought.isFinal() == true` (LLM returns FINISH action)
- Maximum iterations reached (prevents infinite loops)

### Context Management

The context maintains conversation state:

- System Prompt
- Message History
- Tool Definitions
- Long-term Memory
- Business State

#### Context Compression

Automatic compression using sliding window + LLM summarization to control token costs:

```
Trigger: messages.size() > compressTriggerSize (default: 20)

Step 1: Sliding Window
  [msg1]...[msg10] --> to compress
  [msg11]...[msg20] --> recent messages (preserved)

Step 2: LLM Summarization
  Submit [msg1]...[msg10] to LLM for concise summary

Step 3: Long-term Memory Extraction
  User preferences --> longTermMemory
  Tool results --> longTermMemory

Result:
  [HISTORY_SUMMARY: LLM summary][msg11][msg12]...[msg20]
```

**Configuration** (`application.yml`):

```yaml
yharness:
  context:
    compression-enabled: true
    sliding-window-size: 10
    compress-trigger-size: 20
    max-messages: 50
```

### Hook System

Observer-pattern based hooks for lifecycle extensibility:

| Hook | Timing |
|------|--------|
| `onAgentStart` | Agent startup |
| `onAgentEnd` | Agent shutdown |
| `onContextInit` | Context initialization |
| `onContextUpdate` | Context update |
| `onMessageReceived` | User message received |
| `onMessageSend` | Message sent |
| `onProviderCall` | Before LLM call |
| `onProviderResponse` | After LLM response |
| `onThoughtGenerated` | Thought generated |
| `onToolCall` | Before tool execution |
| `onToolResult` | After tool execution |
| `onReActStart` | ReAct loop start |
| `onReActEnd` | ReAct loop end |
| `onError` | Error occurred |

**Built-in Hooks**:
- `LoggingHook` - Logging
- `MetricsHook` - Metrics collection

### Provider Support

Direct API integration without framework abstraction:

| Provider | Description |
|----------|-------------|
| OpenAI | OpenAI and compatible APIs (DeepSeek, etc.) |
| Anthropic | Claude models |

### Tool System

Extensible tool interface:

```java
public interface Tool {
    String getName();
    String getDescription();
    ToolDefinition getDefinition();
    String execute(AgentContext context, Map<String, Object> arguments);
}
```

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 1.8 |
| Framework | SpringBoot | 2.7.x |
| HTTP Client | OkHttp | 4.12.x |
| JSON | Jackson | 2.15.x |
| Logging | SLF4J + Logback | - |

## API Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/agent/health` | GET | Health check |
| `/api/agent/chat` | POST | Send message |
| `/api/agent/context/{id}` | GET | Get context |
| `/api/agent/context/{id}` | DELETE | Clear context |

## Extending YHarness

### Add a New Tool

```java
@Component
public class MyTool implements Tool {
    @Override
    public String getName() {
        return "myTool";
    }

    @Override
    public String getDescription() {
        return "Description for LLM to understand when to use";
    }

    @Override
    public ToolDefinition getDefinition() {
        // Define parameters
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        // Tool logic
        return "result";
    }
}
```

### Add a New Provider

Implement `AIProvider` interface and register with `ProviderFactory`.

### Add a New Hook

Implement `AgentHook` interface and register with `HookManager`.

## License

MIT
