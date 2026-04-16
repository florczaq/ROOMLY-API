package org.roomly.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.roomly.dto.ProductDTO;
import org.roomly.repositories.ProductsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductsService {
    private final ProductsRepository productsRepository;
    private final ExternalApiService externalApiService;
    
    //TODO : implement caching mechanism to avoid redundant API calls for the same barcode
    //TODO implement database storage for products to allow for user-added products and faster retrieval of frequently accessed products
    public ProductDTO getProduct (String barcode) {
        var json = externalApiService.fetchProductData(barcode);
        return new ProductDTO(
          barcode,
          this.extractName(json),
          this.extractBrand(json),
          this.extractQuantity(json)
        );
    }
    
    public ProductDTO getProduct (String barcode, String key) {
        var json = externalApiService.fetchProductData(barcode);
        var value = json.path("product").path(key).asText();
        return new ProductDTO(
          barcode,
          key.equals("product_name") ? value : null,
          key.equals("brands") ? value : null,
          key.equals("quantity") ? value : null
        );
    }
    
    public String addProduct () {
        return "ProductsService";
    }
    
    public String updateProduct () {
        return "ProductsService";
    }
    
    public String deleteProduct () {
        return "ProductsService";
    }
    
    private String extractName (JsonNode json) {
        String name = json.path("product").path("product_name").asText();
        if (name.isEmpty()) {
            return "Unknown";
        }
        return name;
    }
    
    private String extractBrand (JsonNode json) {
        String brand = json.path("product").path("brands").asText();
        if (brand.isEmpty()) {
            return "Unknown";
        }
        return brand;
    }
    
    private String extractQuantity (JsonNode json) {
        String quantity = json.path("product").path("quantity").asText();
        if (quantity.isEmpty()) {
            return "Unknown";
        }
        return quantity;
    }
    
}

