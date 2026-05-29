package org.roomly.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the Open Food Facts external product API.
 * <p>
 * Wraps barcode-based product lookups and returns the raw JSON response as a
 * {@link JsonNode} for downstream field extraction. Uses a reactive {@link WebClient}
 * and blocks synchronously so callers remain on standard servlet threads.
 * </p>
 */
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
    
    /**
     * Fetches product data from the external API for the given barcode.
     *
     * @param barcode the product barcode to look up
     * @return the parsed JSON response tree
     * @throws IllegalArgumentException if the API response cannot be parsed as JSON
     */
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
