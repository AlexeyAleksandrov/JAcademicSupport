package io.github.alexeyaleksandrov.jacademicsupport.services.ollama;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OllamaService
{
    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    private final OllamaApi ollamaApi;

    public OllamaService(OllamaApi ollamaApi) {
        this.ollamaApi = ollamaApi;
    }

    public String chat(String content) {
        // Sync request
        var request = OllamaApi.ChatRequest.builder(model)
                .withStream(false) // not streaming
                .withMessages(List.of(
                        OllamaApi.Message.builder(OllamaApi.Message.Role.SYSTEM)
                                .withContent("Ты ИИ-помощник, который помогает людям находить информацию. " +
                                        "Ты отвечаешь всегда только на русском языке.")
                                .build(),
                        OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                                .withContent(content)
                                .build()))
                .withOptions(OllamaOptions.create().withTemperature(0.9f))  // 0.8f
                .build();

        OllamaApi.ChatResponse response = ollamaApi.chat(request);

        return response.message().content();
    }
}
