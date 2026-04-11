package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.ShoppingListService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ShoppingListResolver {
    private final ShoppingListService shoppingListService;
    
    public String shoppingList () {
        return "ShoppingListResolver";
    }
    
    public String addShoppingList () {
        return "ShoppingListResolver";
    }
    
    public String updateShoppingList () {
        return "ShoppingListResolver";
    }
    
    public String deleteShoppingList () {
        return "ShoppingListResolver";
    }
    
}

