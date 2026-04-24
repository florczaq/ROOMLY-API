package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.InventoryDTO;
import org.roomly.entities.Inventory;
import org.roomly.services.InventoryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InventoryResolver {
    private final InventoryService inventoryService;
    
    @QueryMapping
    public InventoryDTO inventory (@Argument int id) {
        return inventoryService.getInventory(id).toDTO();
    }
    
    @QueryMapping
    public List<InventoryDTO> allInventories (@Argument String householdId) {
        return inventoryService
          .getInventories(householdId)
          .stream()
          .map(Inventory::toDTO)
          .toList();
    }
    
}

