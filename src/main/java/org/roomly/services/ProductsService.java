package org.roomly.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.roomly.annotations.ValidBarcode;
import org.roomly.entities.Product;
import org.roomly.repositories.ProductsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductsService {
    private final ProductsRepository productsRepository;
    private final ExternalApiService externalApiService;
    private static final String MISSING_VALUE = "Unknown";
    private static final String PRODUCT_PATH = "product";
    
    public Product fetchProduct (@ValidBarcode String barcode) {
        var json = externalApiService.fetchProductData(barcode);
        return productsRepository.save(
          new Product().setBarcode(barcode)
            .setName(extractName(json))
            .setBrand(extractBrand(json))
            .setQuantity(extractQuantity(json)));
    }
    
    private String extractName (JsonNode json) {
        String name = json.path(PRODUCT_PATH).path("product_name").asText();
        if (name.isEmpty()) {
            return MISSING_VALUE;
        }
        return name;
    }
    
    private String extractBrand (JsonNode json) {
        String brand = json.path(PRODUCT_PATH).path("brands").asText();
        if (brand.isEmpty()) {
            return MISSING_VALUE;
        }
        return brand;
    }
    
    private String extractQuantity (JsonNode json) {
        String quantity = json.path(PRODUCT_PATH).path("quantity").asText();
        if (quantity.isEmpty()) {
            return MISSING_VALUE;
        }
        return quantity;
    }
    
    @Cacheable(value = "products", key = "#barcode")
    public Product getProductByBarcode (@ValidBarcode String barcode) {
        var product = productsRepository.findByBarcode(barcode);
        return product.orElseGet(() -> this.fetchProduct(barcode));
    }
}

