package io.github.alexeyaleksandrov.jacademicsupport.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Utility class for reading files from resources directory.
 */
@Component
@Slf4j
public class ResourceFileReader {
    
    /**
     * Reads the content of a file from the classpath resources.
     * 
     * @param resourcePath Path to the resource file relative to resources directory
     * @return Content of the file as a String
     * @throws IOException if the file cannot be read
     */
    public String readResourceFile(String resourcePath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                log.debug("Successfully read resource file: {}", resourcePath);
                return content;
            }
        } catch (IOException e) {
            log.error("Failed to read resource file: {}", resourcePath, e);
            throw new IOException("Failed to read resource file: " + resourcePath, e);
        }
    }
}
