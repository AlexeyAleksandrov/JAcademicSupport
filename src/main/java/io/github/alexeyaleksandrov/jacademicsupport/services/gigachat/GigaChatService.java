package io.github.alexeyaleksandrov.jacademicsupport.services.gigachat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexeyaleksandrov.jacademicsupport.services.llm.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * GigaChat implementation of LlmService.
 * Integrates with Sber's GigaChat API for language model operations.
 * Note: SSL verification is disabled to work with GigaChat's self-signed certificates.
 */
@Service
@Slf4j
public class GigaChatService implements LlmService {
    
    @Value("${gigachat.api.url:https://gigachat.devices.sberbank.ru/api/v1}")
    private String apiUrl;
    
    @Value("${gigachat.oauth.url:https://ngw.devices.sberbank.ru:9443/api/v2/oauth}")
    private String oauthUrl;
    
    @Value("${gigachat.api.token}")
    private String authorizationToken;
    
    @Value("${gigachat.model:GigaChat}")
    private String model;
    
    @Value("${gigachat.scope:GIGACHAT_API_PERS}")
    private String scope;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache for access token
    private String cachedAccessToken;
    private long tokenExpirationTime = 0;
    
    public GigaChatService() {
        this.restTemplate = createRestTemplateWithDisabledSsl();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Creates a RestTemplate with SSL verification disabled.
     * This is necessary for GigaChat API which uses self-signed certificates.
     * WARNING: This should only be used in development. For production, proper certificate handling is required.
     */
    private RestTemplate createRestTemplateWithDisabledSsl() {
        try {
            SSLContext sslContext = SSLContextBuilder
                    .create()
                    .loadTrustMaterial(new TrustAllStrategy())
                    .build();
            
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
            
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                            .setSSLSocketFactory(sslSocketFactory)
                            .build())
                    .build();
            
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            
            return new RestTemplate(factory);
        } catch (Exception e) {
            log.error("Failed to create RestTemplate with disabled SSL: {}", e.getMessage(), e);
            return new RestTemplate(); // Fallback to default
        }
    }
    
    /**
     * Obtains an access token from GigaChat OAuth endpoint.
     * The token is cached and reused until it expires.
     */
    private String getAccessToken() {
        // Check if we have a valid cached token
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            log.debug("Using cached GigaChat access token");
            return cachedAccessToken;
        }
        
        try {
            log.debug("Requesting new access token from GigaChat OAuth");
            
            // Prepare OAuth request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            headers.set("RqUID", java.util.UUID.randomUUID().toString());
            headers.set("Authorization", "Basic " + authorizationToken);
            
            // Prepare OAuth request body
            String body = "scope=" + scope;
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            // Make OAuth request
            ResponseEntity<Map> response = restTemplate.exchange(
                oauthUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("access_token");
                
                if (accessToken != null) {
                    cachedAccessToken = accessToken;
                    
                    // Set expiration time (default to 30 minutes if not provided)
                    Object expiresIn = responseBody.get("expires_at");
                    if (expiresIn != null) {
                        tokenExpirationTime = ((Number) expiresIn).longValue();
                    } else {
                        // Default: token expires in 30 minutes
                        tokenExpirationTime = System.currentTimeMillis() + (30 * 60 * 1000);
                    }
                    
                    log.debug("Successfully obtained GigaChat access token");
                    return accessToken;
                }
            }
            
            log.error("Failed to obtain access token from GigaChat OAuth");
            throw new RuntimeException("Failed to obtain access token from GigaChat OAuth");
            
        } catch (Exception e) {
            log.error("Error obtaining GigaChat access token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with GigaChat: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String chat(String content) {
        // Use default system prompt and model for backward compatibility
        return chat(content, null, null);
    }
    
    /**
     * Sends a chat request to GigaChat API with custom system prompt and model.
     * 
     * @param content User message content
     * @param systemPrompt Custom system prompt (null for default)
     * @param modelName Model name to use (null for default from config)
     * @return AI response content
     */
    public String chat(String content, String systemPrompt, String modelName) {
        try {
            // Get valid access token
            String accessToken = getAccessToken();
            
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.setBearerAuth(accessToken);
            
            // Use provided values or defaults
            String effectiveModel = (modelName != null && !modelName.isEmpty()) ? modelName : model;
            String effectiveSystemPrompt = (systemPrompt != null && !systemPrompt.isEmpty()) 
                ? systemPrompt 
                : "Ты ИИ-помощник, который помогает людям находить информацию. " +
                  "Ты отвечаешь всегда только на русском языке.";
            
            // Prepare request body
            Map<String, Object> requestBody = Map.of(
                "model", effectiveModel,
                "messages", List.of(
                    Map.of(
                        "role", "system",
                        "content", effectiveSystemPrompt
                    ),
                    Map.of(
                        "role", "user",
                        "content", content
                    )
                ),
                "temperature", 0.01,
                "top_p", 0.01,
                "max_tokens", 2048,
                "profanity_check", true
            );
            
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);
            
            // Make API call
            String endpoint = apiUrl + "/chat/completions";
            log.debug("Calling GigaChat API at: {}", endpoint);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            // Parse response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    
                    if (message != null && message.containsKey("content")) {
                        String responseContent = (String) message.get("content");
                        log.debug("Successfully received response from GigaChat");
                        return responseContent;
                    }
                }
            }
            
            log.error("Failed to parse valid response from GigaChat API. Response status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to parse response from GigaChat API");
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP 4xx errors (authentication, bad request, etc.)
            log.error("GigaChat API client error ({}): {}. Check your API token and request format.", 
                     e.getStatusCode(), e.getMessage());
            throw new RuntimeException("GigaChat API authentication or request error: " + e.getStatusCode(), e);
            
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // HTTP 5xx errors (server issues)
            log.error("GigaChat API server error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("GigaChat API server error: " + e.getStatusCode(), e);
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Network/connection errors (including SSL)
            log.error("Failed to connect to GigaChat API: {}. Check network connection and SSL configuration.", 
                     e.getMessage());
            throw new RuntimeException("Failed to connect to GigaChat API. Check network and SSL configuration.", e);
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // JSON serialization errors
            log.error("Failed to serialize request to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize GigaChat request", e);
            
        } catch (Exception e) {
            // Other unexpected errors
            log.error("Unexpected error calling GigaChat API: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error calling GigaChat API: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getProviderName() {
        return "gigachat";
    }
}
