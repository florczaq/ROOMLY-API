package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.entities.Inventory;
import org.roomly.entities.Profile;
import org.roomly.repositories.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    
    public Inventory getInventory (int id) {
        return inventoryRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException("Inventory [" + id + "] not found"));
    }
    
    public Inventory createInventory (@Nullable Profile user) {
        return inventoryRepository.save(new Inventory().setOwner(user));
    }
    
    public List<Inventory> getInventories (String householdId) {
        return inventoryRepository.findAllByHouseholdId(householdId);
    }
}

