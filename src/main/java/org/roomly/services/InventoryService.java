package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.annotations.Notifiable;
import org.roomly.entities.*;
import org.roomly.repositories.InventoryItemRepository;
import org.roomly.repositories.InventoryRepository;
import org.roomly.repositories.ProductsRepository;
import org.roomly.repositories.ProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductsRepository productsRepository;
    private final ProfileRepository profileRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public Inventory getInventory(int id) {
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Inventory [" + id + "] not found"));
    }

    public Inventory createInventory(@Nullable Profile user) {
        return inventoryRepository.save(new Inventory().setOwner(user));
    }

    public List<Inventory> getInventories(String householdId) {
        return inventoryRepository.findAllByHouseholdId(householdId);
    }

    @Notifiable(
        title = "Product '#{#result.product.name} added to your inventory",
        description = "#{#result.addedBy.nickname} added '#{#result.product.name}' to your inventory",
        recipientProfileId = "#{#result.inventory.owner.id}"
    )
    @Transactional
    public InventoryItem addProductToInventory(int productId,
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

        Optional<InventoryItem> inventoryItem = inventoryItemRepository.findByInventoryAndProduct(inventory, product);
        if (inventoryItem.isPresent()) {
            inventoryItem.get().setCount(inventoryItem.get().getCount() + count);
            if (notes != null && !notes.isEmpty()) {
                inventoryItem.get().setNotes(notes);
            }
            return inventoryItemRepository.save(inventoryItem.get());
        }

        var newInventoryItem = new InventoryItem()
            .setProduct(product)
            .setCount(count)
            .setNotes(notes)
            .setAddedBy(addedBy)
            .setInventory(inventory);

        inventory.getItems().add(newInventoryItem);
        inventoryRepository.save(inventory);

        return newInventoryItem;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    @Notifiable(
        title = "Product '#{#result.product.name}' removed from your inventory",
        description = "#{#result.addedBy.nickname} removed '#{#result.product.name}' from your inventory",
        recipientProfileId = "#{#result.inventory.owner.id}"
    )
    public InventoryItem removeProductFromInventory(int productId,
                                                    int shoppingListId,
                                                    int count,
                                                    @Nullable String notes
    ) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }

        Product product = productsRepository
            .findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Inventory inventory = inventoryRepository
            .findById(shoppingListId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        InventoryItem inventoryItem = inventoryItemRepository
            .findByInventoryAndProduct(inventory, product)
            .orElseThrow(() -> new EntityNotFoundException("Product is not present in inventory"));

        int updatedCount = inventoryItem.getCount() - count;

        if (updatedCount > 0) {
            inventoryItem.setCount(updatedCount);
            if (notes != null && !notes.isEmpty()) {
                inventoryItem.setNotes(notes);
            }
        } else {
            inventory.getItems().remove(inventoryItem);
        }


        inventoryRepository.save(inventory);
        return inventoryItem;
    }

    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return authentication.getName();
    }
}

