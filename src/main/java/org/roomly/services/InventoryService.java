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
    
    public int createInventory (@Nullable Profile user, @NotNull Household household) {
        Inventory savedInventory =
          inventoryRepository.save(
            new Inventory()
              .setHousehold(household)
              .setOwner(user)
          );
        return savedInventory.getId();
    }
    
    public String updateInventory () {
        return "InventoryService";
    }
    
    public String deleteInventory () {
        return "InventoryService";
    }
    
}

