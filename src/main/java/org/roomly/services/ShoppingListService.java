package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.ShoppingListRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    
    public String getShoppingList () {
        return "ShoppingListService";
    }
    
    public String addShoppingList () {
        return "ShoppingListService";
    }
    
    public String updateShoppingList () {
        return "ShoppingListService";
    }
    
    public String deleteShoppingList () {
        return "ShoppingListService";
    }
    
}

