package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.InventoryService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InventoryResolver {
    private final InventoryService inventoryService;
    
    public String inventory () {
        return "InventoryResolver";
    }
    
    public String addInventory () {
        return "InventoryResolver";
    }
    
    public String updateInventory () {
        return "InventoryResolver";
    }
    
    public String deleteInventory () {
        return "InventoryResolver";
    }
    
}

