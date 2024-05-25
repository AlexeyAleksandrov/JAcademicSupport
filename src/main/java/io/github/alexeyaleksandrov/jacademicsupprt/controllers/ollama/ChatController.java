package io.github.alexeyaleksandrov.jacademicsupprt.controllers.ollama;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final OllamaChatClient chatClient;

    @Autowired
    public ChatController(OllamaChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatClient.call(message));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatClient.stream(prompt);
    }

    @GetMapping("/ask")
    public String answer()
    {
        OllamaApi ollamaApi =
                new OllamaApi("http://localhost:11434");

        String competency = "ОПК-3.2 : Решает задачи профессиональной деятельности с применением современных информационно-коммуникационных технологий с учетом требований информационной безопасности. Знать: - Выбирает информационные технологии для профессиональной деятельности с учетом требований безопасности. Уметь: - Идентифицирует требования к информационной безопасности в профессиональной деятельности. Владеть: - Применяет технологии обеспечения информационной безопасности.";

        // Sync request
        var request = OllamaApi.ChatRequest.builder("openchat")
                .withStream(false) // not streaming
                .withMessages(List.of(
                        OllamaApi.Message.builder(OllamaApi.Message.Role.SYSTEM)
                                .withContent("Ты ИИ-помощник, который помогает людям находить информацию. Ты отвечаешь всегда только на русском языке.")
                                .build(),
                        OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                                .withContent("Выдели основные ключевые слова из описания профессиональной компетенции: " + competency + "Исключи из ответа размытые и обобщённые словосочетания. Ответ должен быть только на русском языке. Все слова в ответе должны находиться в нормальной форме без склонений и спряжений.  В ответе не пиши словоочетание \\\"ключевые слова\\\", напиши только сами слова")
                                .build()))
                .withOptions(OllamaOptions.create().withTemperature(0.9f))
                .build();

        OllamaApi.ChatResponse response = ollamaApi.chat(request);

        return response.message().content();
    }

}
