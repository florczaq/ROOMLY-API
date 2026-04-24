package org.roomly.services;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.entities.Household;
import org.roomly.entities.Inventory;
import org.roomly.entities.Profile;
import org.roomly.repositories.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    
    public String getInventory () {
        return "InventoryService";
    }
    
    public Inventory createInventory (@Nullable Profile user, @NotNull Household household) {
        return inventoryRepository.save(new Inventory().setOwner(user));
    }
    
    public String updateInventory () {
        return "InventoryService";
    }
    
    public String deleteInventory () {
        return "InventoryService";
    }
    
}

