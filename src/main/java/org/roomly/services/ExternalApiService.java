package org.roomly.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ExternalApiService {
    private final String apiUrl;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public ExternalApiService (
      @Value("${external.api.openfoodfacts.url}") String apiUrl,
      WebClient.Builder webClientBuilder,
      ObjectMapper objectMapper
    ) {
        this.apiUrl = apiUrl;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }
    
    public JsonNode fetchProductData (String barcode) {
        String jsonResponse = webClient
          .get()
          .uri(apiUrl + "/" + barcode + ".json")
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<String>() {})
          .block();
        
        try {
            return objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse product data for barcode: " + barcode, e);
        }
    }
}
