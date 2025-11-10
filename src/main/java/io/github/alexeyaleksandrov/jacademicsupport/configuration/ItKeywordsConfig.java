package io.github.alexeyaleksandrov.jacademicsupport.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for loading IT-related keywords from it-keywords.yml.
 * These keywords are used to identify IT-related vacancies.
 */
@Configuration
@ConfigurationProperties(prefix = "vacancy")
@Getter
@Setter
public class ItKeywordsConfig {
    
    /**
     * List of IT-related keywords.
     * A vacancy is considered IT-related if its name contains at least one of these keywords (case-insensitive).
     */
    private List<String> itKeywords;
}
