package io.github.alexeyaleksandrov.jacademicsupport.services.llm;

/**
 * Interface for Language Model services.
 * Provides abstraction over different LLM providers (Ollama, GigaChat, etc.)
 */
public interface LlmService {
    
    /**
     * Send a chat message to the LLM and get a response
     * 
     * @param content The user's message content
     * @return The LLM's response
     */
    String chat(String content);
    
    /**
     * Get the name/type of the LLM provider
     * 
     * @return Provider name (e.g., "ollama", "gigachat")
     */
    String getProviderName();
}
