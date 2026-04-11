package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.ProductsService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductsResolver {
    private final ProductsService productsService;
    
    public String products () {
        return "ProductsResolver";
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

