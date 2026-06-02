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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing household and personal inventories and the products within them.
 * <p>
 * Each profile and the household itself own an {@link Inventory}. Products are added with
 * a count (incremented if the item already exists) or removed, decrementing the count and
 * deleting the {@link InventoryItem} when it reaches zero. Push notifications are sent to
 * the inventory owner on add and remove operations.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductsRepository productsRepository;
    private final ProfileRepository profileRepository;
    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Returns the inventory with the given ID.
     *
     * @param id inventory ID
     * @return the matching {@link Inventory}
     * @throws EntityNotFoundException if no inventory exists with the given ID
     */
    public Inventory getInventory(int id) {
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Inventory [" + id + "] not found"));
    }

    /**
     * Creates and persists a new inventory optionally owned by a profile.
     * Pass {@code null} for a shared (household-level) inventory.
     *
     * @param user the owning profile, or {@code null} for a shared inventory
     * @return the persisted {@link Inventory}
     */
    public Inventory createInventory(@Nullable Profile user) {
        return inventoryRepository.save(new Inventory().setOwner(user));
    }

    /**
     * Returns all inventories (shared and personal) belonging to a household.
     *
     * @param householdId ID of the household
     * @return list of inventories in the household
     */
    public List<Inventory> getInventories(String householdId) {
        return inventoryRepository.findAllByHouseholdId(householdId);
    }

    /**
     * Adds a product to an inventory, incrementing the count if the product is already present.
     * If a non-empty {@code notes} value is provided it overwrites any existing notes.
     * Sends a push notification to the inventory owner on success.
     *
     * @param productId   ID of the product to add
     * @param inventoryId ID of the target inventory
     * @param count       quantity to add (must be positive)
     * @param notes       optional notes to attach to the item
     * @return the created or updated {@link InventoryItem}
     * @throws EntityNotFoundException if the product, inventory, or resolved profile does not exist
     */
    @Notifiable(
        title = "Product '#{#result.product.name} added to your inventory",
        description = "#{#result.addedBy.nickname} added '#{#result.product.name}' to your inventory",
        recipientProfileId = "#{#result.inventory.owner.id}"
    )
    @Transactional
    public InventoryItem addProductToInventory(int productId,
                                               int inventoryId,
                                               int count,
                                               @Nullable String notes,
                                               String accountId
    ) {
        Product product = productsRepository
            .findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Inventory inventory = this.getInventory(inventoryId);

        String householdId = inventoryRepository.findHouseholdIdById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Household not found for inventory: " + inventoryId));

        Profile addedBy = profileRepository
            .findByHouseholdIdAndAccountId(householdId, accountId)
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

    /**
     * Removes a quantity of a product from an inventory. If the resulting count is zero or
     * below, the {@link InventoryItem} is deleted entirely. Sends a push notification to the
     * inventory owner on success.
     *
     * @param productId      ID of the product to remove
     * @param shoppingListId ID of the inventory (parameter name is misleading — it is an inventory ID)
     * @param count          quantity to remove (must be greater than 0)
     * @param notes          optional notes to attach if the item remains
     * @return the updated (or about-to-be-deleted) {@link InventoryItem}
     * @throws IllegalArgumentException if {@code count} is not positive
     * @throws EntityNotFoundException  if the product, inventory, or item is not found
     */
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

}

