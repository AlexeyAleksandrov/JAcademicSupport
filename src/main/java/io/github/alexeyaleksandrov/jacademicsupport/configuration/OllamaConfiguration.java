package io.github.alexeyaleksandrov.jacademicsupport.configuration;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class OllamaConfiguration {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaUrl;

    @Bean
    protected OllamaApi ollamaApi() {
        return new OllamaApi(ollamaUrl);
    }
}
