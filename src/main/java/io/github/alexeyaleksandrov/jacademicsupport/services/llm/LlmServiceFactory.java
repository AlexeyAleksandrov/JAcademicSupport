package io.github.alexeyaleksandrov.jacademicsupport.services.llm;

import io.github.alexeyaleksandrov.jacademicsupport.services.gigachat.GigaChatService;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating and selecting LLM service implementations
 * based on the requested provider name.
 */
@Component
@AllArgsConstructor
public class LlmServiceFactory {
    
    private final OllamaService ollamaService;
    private final GigaChatService gigaChatService;
    
    /**
     * Get the appropriate LLM service based on the provider name
     * 
     * @param providerName The name of the provider (e.g., "ollama", "gigachat")
     * @return The corresponding LlmService implementation
     * @throws IllegalArgumentException if the provider is not supported
     */
    public LlmService getService(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }
        
        String normalizedProvider = providerName.toLowerCase().trim();
        
        switch (normalizedProvider) {
            case "ollama":
                return ollamaService;
            case "gigachat":
                return gigaChatService;
            default:
                throw new IllegalArgumentException(
                    "Unsupported LLM provider: " + providerName + 
                    ". Supported providers: ollama, gigachat"
                );
        }
    }
    
    /**
     * Get all available LLM providers
     * 
     * @return Map of provider names to their service implementations
     */
    public Map<String, LlmService> getAllServices() {
        Map<String, LlmService> services = new HashMap<>();
        services.put("ollama", ollamaService);
        services.put("gigachat", gigaChatService);
        return services;
    }
}
