package io.github.alexeyaleksandrov.jacademicsupport.services.ollama;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class OllamaServiceDisabled extends OllamaService {
    public OllamaServiceDisabled() {
        super(null); // ollamaApi не нужен, так как сервис не работает
    }

    @Override
    public String chat(String content) {
        throw new IllegalStateException("OllamaService недоступен в production-среде!");
    }
}
