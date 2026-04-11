package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.ProductsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductsService {
    private final ProductsRepository productsRepository;
    
    public String getProducts () {
        return "ProductsService";
    }
    
    public String addProducts () {
        return "ProductsService";
    }
    
    public String updateProducts () {
        return "ProductsService";
    }
    
    public String deleteProducts () {
        return "ProductsService";
    }
    
}

