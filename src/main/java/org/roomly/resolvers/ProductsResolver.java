package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.ProductsService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductsResolver {
    private final ProductsService productsService;

}

