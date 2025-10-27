# LLM Integration Guide

## Overview

This application supports multiple Large Language Model (LLM) providers for generating keywords from competency descriptions. The implementation uses a flexible, interface-based architecture that allows easy integration of new LLM providers.

## Supported LLM Providers

Currently supported providers:
- **Ollama** (local LLM)
- **GigaChat** (Sber's cloud-based LLM)

## Architecture

### Components

1. **LlmService Interface** (`services/llm/LlmService.java`)
   - Provides abstraction over different LLM providers
   - Methods: `chat(String content)`, `getProviderName()`

2. **OllamaService** (`services/ollama/OllamaService.java`)
   - Implementation for local Ollama LLM
   - Configured via `application.properties`

3. **GigaChatService** (`services/gigachat/GigaChatService.java`)
   - Implementation for Sber GigaChat API
   - Requires access token configuration

4. **LlmServiceFactory** (`services/llm/LlmServiceFactory.java`)
   - Factory class to select appropriate LLM service based on provider name
   - Validates provider names and provides error handling

## Configuration

### Ollama Configuration

Add the following to `application.properties`:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=openchat
spring.ai.ollama.chat.options.temperature=0.7
```

### GigaChat Configuration

Add the following to `application.properties`:

```properties
gigachat.api.url=https://gigachat.devices.sberbank.ru/api/v1
gigachat.api.token=YOUR_GIGACHAT_ACCESS_TOKEN
gigachat.model=GigaChat
```

**Important:** Replace `YOUR_GIGACHAT_ACCESS_TOKEN` with your actual GigaChat access token.

## API Usage

### Generate Keywords for Competency

**Endpoint:** `POST /api/competencies/{id}/keywords/generate`

**Parameters:**
- `id` (path variable) - Competency ID
- `model` (query parameter, optional) - LLM provider to use
  - Default: `ollama`
  - Supported values: `ollama`, `gigachat`

**Examples:**

Using Ollama (default):
```bash
POST /api/competencies/1/keywords/generate
```

Using Ollama explicitly:
```bash
POST /api/competencies/1/keywords/generate?model=ollama
```

Using GigaChat:
```bash
POST /api/competencies/1/keywords/generate?model=gigachat
```

**Response:**
Returns `CompetencyDto` with generated keywords.

## Adding New LLM Providers

To add a new LLM provider:

1. **Create Service Implementation**
   - Create a new service class implementing `LlmService` interface
   - Implement `chat(String content)` method with provider-specific logic
   - Implement `getProviderName()` to return unique provider identifier

2. **Update LlmServiceFactory**
   - Add the new service as a dependency in the constructor
   - Add a new case in the `getService()` switch statement
   - Add the service to `getAllServices()` method

3. **Add Configuration**
   - Add provider-specific configuration properties to `application.properties`

4. **Update Documentation**
   - Update this file with the new provider information

## Example: New LLM Provider Implementation

```java
@Service
public class MyNewLlmService implements LlmService {
    
    @Value("${mynewllm.api.token}")
    private String apiToken;
    
    @Override
    public String chat(String content) {
        // Implementation specific to your LLM provider
        return "response";
    }
    
    @Override
    public String getProviderName() {
        return "mynewllm";
    }
}
```

Then update `LlmServiceFactory`:

```java
@Component
@AllArgsConstructor
public class LlmServiceFactory {
    private final OllamaService ollamaService;
    private final GigaChatService gigaChatService;
    private final MyNewLlmService myNewLlmService; // Add new service
    
    public LlmService getService(String providerName) {
        switch (providerName.toLowerCase()) {
            case "ollama": return ollamaService;
            case "gigachat": return gigaChatService;
            case "mynewllm": return myNewLlmService; // Add new case
            default: throw new IllegalArgumentException("Unsupported provider");
        }
    }
}
```

## Error Handling

The factory throws `IllegalArgumentException` if:
- Provider name is null or empty
- Provider name is not supported

Make sure to handle these exceptions appropriately in your client code.

## Security Notes

- **Never commit access tokens** to version control
- Use environment variables or secure configuration management for tokens
- GigaChat tokens should be kept secure and rotated regularly
