# Artificial Intelligence (AI) Abstraction

## 1. Motivation

Yangiariq Aqua AI harnesses Large Language Models (LLMs) to analyze soil conditions, crop stage, and regional climate patterns, providing actionable irrigation advice to farmers and operators.

Direct coupling between REST controllers or application services and specific vendor SDKs (e.g. Gemini, OpenAI, Anthropic) introduces significant vendor lock-in risks, fragile testing, and billing vulnerabilities.

## 2. Abstraction Design

The application follows the **Dependency Inversion Principle (DIP)**:

```
[Application Layer]
       |
       v
+-------------------------------------------------------------+
| <<interface>> com.aquaai.ai.domain.AiProvider              |
| + getProviderName(): String                                 |
| + generateIrrigationRecommendation(req): AiRecResult        |
| + analyzeWaterProblem(desc, loc): String                    |
+-------------------------------------------------------------+
               ^                               ^
               |                               |
    [Infrastructure Adapter]         [Infrastructure Adapter]
+-----------------------------+   +-----------------------------+
| GeminiAiProvider            |   | MockAiProvider              |
| - Connects via HTTP API     |   | - Local deterministic mock  |
| - Condition: 'gemini'       |   | - Default for tests & dev   |
+-----------------------------+   +-----------------------------+
```

## 3. Configuration & Switching

Switching providers is purely configuration-driven via `application.yml` or environment variables:

```yaml
app:
  ai:
    provider: ${AI_PROVIDER:mock}   # Options: 'mock', 'gemini'
    gemini:
      api-key: ${GEMINI_API_KEY}
      model: ${GEMINI_MODEL:gemini-1.5-flash}
```

- When `AI_PROVIDER=mock`, the application activates `MockAiProvider`, requiring zero API keys and zero internet access.
- When `AI_PROVIDER=gemini`, the Spring Boot container injects `GeminiAiProvider`.
- Additional providers (e.g. `OllamaAiProvider`, `OpenAiProvider`) can be added simply by implementing `AiProvider` without modifying existing business services.
