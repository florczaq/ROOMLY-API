package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.annotations.ValidBarcode;
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
    public ProductDTO product (@Argument @ValidBarcode String barcode) {
        return productsService.getProductByBarcode(barcode).toDTO();
    }
    
}

