package io.github.alexeyaleksandrov.jacademicsupport.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешить запросы с localhost:3000
        config.addAllowedOrigin("http://localhost:3000");

        // Также можно разрешить все origins (НЕ рекомендуется для production)
         config.addAllowedOrigin("*");

        // Разрешенные методы
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Разрешенные заголовки
        config.addAllowedHeader("*");

        // Разрешить credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}