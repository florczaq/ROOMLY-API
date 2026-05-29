package org.roomly.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.roomly.annotations.ValidBarcode;
import org.roomly.entities.Product;
import org.roomly.repositories.ProductsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for product lookup and persistence.
 * <p>
 * Products are identified by barcode. When a barcode is not found in the local database,
 * product data is fetched from the Open Food Facts API via {@link ExternalApiService} and
 * persisted for future use. Results are cached by barcode to reduce database and network
 * overhead. Missing fields in the external response fall back to {@code "Unknown"}.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ProductsService {
    private final ProductsRepository productsRepository;
    private final ExternalApiService externalApiService;
    private static final String MISSING_VALUE = "Unknown";
    private static final String PRODUCT_PATH = "product";

    /**
     * Fetches product data from the external API for the given barcode and persists it.
     * Called automatically by {@link #getProductByBarcode} on a cache miss.
     *
     * @param barcode a valid product barcode
     * @return the persisted {@link Product}
     */
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
    
    /**
     * Returns a product by barcode, fetching and persisting it from the external API on a cache miss.
     * Results are cached under the {@code products} cache keyed by barcode.
     *
     * @param barcode a valid product barcode
     * @return the matching {@link Product}
     */
    @Cacheable(value = "products", key = "#barcode")
    public Product getProductByBarcode (@ValidBarcode String barcode) {
        var product = productsRepository.findByBarcode(barcode);
        return product.orElseGet(() -> this.fetchProduct(barcode));
    }
}
