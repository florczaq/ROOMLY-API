package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.annotations.Notifiable;
import org.roomly.entities.*;
import org.roomly.repositories.InventoryRepository;
import org.roomly.repositories.ProductsRepository;
import org.roomly.repositories.ProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductsRepository productsRepository;
    private final ProfileRepository profileRepository;
    
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
    
    //TODO finish this method - add product to shopping list, create notification for household members, etc.
    @Notifiable(
      title = "Product '#{#result.product.name} added to your inventory",
      recipientProfileId = "#{#result.inventory.owner.id}"
    )
    @Transactional
    public InventoryItem addProductToInventory (int productId,
      int inventoryId,
      int count,
      @Nullable String notes
    ) {
        Product product = productsRepository
          .findById(productId)
          .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        
        Inventory inventory = this.getInventory(inventoryId);
        String accountId = getAuthenticatedUserId();
        
        Profile addedBy = profileRepository
          .findByHouseholdIdAndAccountId(inventory.getOwner().getHousehold().getId(), accountId)
          .orElseThrow(() -> new EntityNotFoundException("Profile not found for user: " + accountId));
        
        InventoryItem inventoryItem = new InventoryItem()
          .setProduct(product)
          .setCount(count)
          .setNotes(notes)
          .setAddedBy(addedBy)
          .setInventory(inventory);
        
        inventory.getItems().add(inventoryItem);
        inventoryRepository.save(inventory);
        
        return inventoryItem;
    }
    
    private String getAuthenticatedUserId () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return authentication.getName();
    }
}

