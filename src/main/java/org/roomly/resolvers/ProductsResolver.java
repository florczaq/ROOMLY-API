package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.ProductDTO;
import org.roomly.services.ProductsService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductsResolver {
    private final ProductsService productsService;
    
    @QueryMapping
    public ProductDTO products (@Argument String barcode, @Argument String key) {
        if (key != null) {
            return productsService.getProduct(barcode, key);
        }
        return productsService.getProduct(barcode);
    }
    
    public String addProducts () {
        return "ProductsResolver";
    }
    
    public String updateProducts () {
        return "ProductsResolver";
    }
    
    public String deleteProducts () {
        return "ProductsResolver";
    }
    
}

