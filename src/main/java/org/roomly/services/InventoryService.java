package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    
    public String getInventory () {
        return "InventoryService";
    }
    
    public String addInventory () {
        return "InventoryService";
    }
    
    public String updateInventory () {
        return "InventoryService";
    }
    
    public String deleteInventory () {
        return "InventoryService";
    }
    
}

