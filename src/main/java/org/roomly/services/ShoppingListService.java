package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.annotations.Notifiable;
import org.roomly.dto.ShoppingListDTO;
import org.roomly.entities.*;
import org.roomly.repositories.ProductsRepository;
import org.roomly.repositories.ProfileRepository;
import org.roomly.repositories.ShoppingListItemRepository;
import org.roomly.repositories.ShoppingListRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing shopping lists and their product items.
 * <p>
 * Each profile and the household share a {@link ShoppingList}. Products are added with a
 * count (incremented if the item already exists) and removed with a decrement that deletes
 * the {@link ShoppingListItem} when its count reaches zero. Push notifications are sent to
 * the shopping list owner on add and remove operations.
 * </p>
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ProductsRepository productsRepository;
    private final ProfileRepository profileRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    /**
     * Returns the shopping list with the given ID.
     *
     * @param id shopping list ID
     * @return the matching {@link ShoppingList}
     * @throws EntityNotFoundException if no shopping list exists with the given ID
     */
    public ShoppingList getShoppingList(int id) {
        return shoppingListRepository.getShoppingListById(id)
            .orElseThrow(() -> new EntityNotFoundException("Shopping list not found"));
    }

    /**
     * Returns DTOs of all shopping lists (shared and personal) belonging to a household.
     *
     * @param household the household to query
     * @return list of shopping list DTOs
     */
    public List<ShoppingListDTO> getAllShoppingLists(@NotNull Household household) {
        List<ShoppingList> shoppingLists = shoppingListRepository.findAllByHousehold(household);
        return shoppingLists.stream().map(ShoppingList::toDTO).toList();
    }

    /**
     * Creates and persists a new shopping list optionally owned by a profile.
     * Pass {@code null} for {@code profile} to create a shared (household-level) shopping list.
     *
     * @param profile   the owning profile, or {@code null} for a shared list
     * @param household the household the list belongs to
     * @return the persisted {@link ShoppingList}
     */
    @Transactional
    public ShoppingList createShoppingList(@Nullable Profile profile, @NotNull Household household) {
        return shoppingListRepository.save(new ShoppingList().setOwner(profile));
    }

    /**
     * Adds a product to a shopping list, incrementing the count if the product is already present.
     * If a non-empty {@code notes} value is provided it overwrites any existing notes.
     * Sends a push notification to the shopping list owner on success.
     *
     * @param productId      ID of the product to add
     * @param shoppingListId ID of the target shopping list
     * @param count          quantity to add (must be positive)
     * @param notes          optional notes to attach to the item
     * @return the created or updated {@link ShoppingListItem}
     * @throws EntityNotFoundException if the product, shopping list, or resolved profile does not exist
     */
    @Notifiable(
        title = "Product '#{#result.product.name} added to your shopping list",
        description = "#{#result.addedBy.nickname} added '#{#result.product.name}' to your shopping list",
        recipientProfileId = "#{#result.shoppingList.owner.id}"
    )
    @Transactional
    public ShoppingListItem addProductToShoppingList(int productId,
                                                     int shoppingListId,
                                                     int count,
                                                     @Nullable String notes
    ) {
        Product product = productsRepository
            .findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ShoppingList shoppingList = shoppingListRepository
            .findById(shoppingListId)
            .orElseThrow(() -> new EntityNotFoundException("Shopping list not found"));

        String accountId = getAuthenticatedUserId();

        Profile addedBy = profileRepository
            .findByHouseholdIdAndAccountId(shoppingList.getOwner().getHousehold().getId(), accountId)
            .orElseThrow(() -> new EntityNotFoundException("Profile not found for user: " + accountId));

        ShoppingListItem shoppingListItem = shoppingListItemRepository
            .findByShoppingListAndProduct(shoppingList, product)
            .orElse(null);

        if (shoppingListItem != null) {
            shoppingListItem.setCount(shoppingListItem.getCount() + count);
            if (notes != null && !notes.isEmpty()) {
                shoppingListItem.setNotes(notes);
            }
        } else {
            shoppingListItem = new ShoppingListItem()
                .setProduct(product)
                .setCount(count)
                .setNotes(notes)
                .setAddedBy(addedBy)
                .setShoppingList(shoppingList);
            shoppingList.getItems().add(shoppingListItem);
        }
        shoppingListRepository.save(shoppingList);

        return shoppingListItem;
    }

    /**
     * Removes a quantity of a product from a shopping list. If the resulting count is zero or
     * below, the {@link ShoppingListItem} is deleted entirely. Sends a push notification to
     * the shopping list owner on success.
     *
     * @param productId      ID of the product to remove
     * @param shoppingListId ID of the target shopping list
     * @param count          quantity to remove (must be greater than 0)
     * @param notes          optional notes to attach if the item remains
     * @return the updated (or about-to-be-deleted) {@link ShoppingListItem}
     * @throws IllegalArgumentException if {@code count} is not positive
     * @throws EntityNotFoundException  if the product, shopping list, or item is not found
     */
    @Transactional
    @Notifiable(
        title = "Product '#{#result.product.name}' removed from your shopping list",
        description = "#{#result.addedBy.nickname} removed '#{#result.product.name}' from your shopping list",
        recipientProfileId = "#{#result.shoppingList.owner.id}"
    )
    public ShoppingListItem removeProductFromShoppingList(int productId,
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

        ShoppingList shoppingList = shoppingListRepository
            .findById(shoppingListId)
            .orElseThrow(() -> new EntityNotFoundException("Shopping list not found"));

        ShoppingListItem shoppingListItem = shoppingListItemRepository
            .findByShoppingListAndProduct(shoppingList, product)
            .orElseThrow(() -> new EntityNotFoundException("Product is not present in shopping list"));

        int updatedCount = shoppingListItem.getCount() - count;

        if (updatedCount > 0) {
            shoppingListItem.setCount(updatedCount);
            if (notes != null && !notes.isEmpty())
                shoppingListItem.setNotes(notes);
        } else {
            shoppingList.getItems().remove(shoppingListItem);
        }

        shoppingListRepository.save(shoppingList);
        return shoppingListItem;
    }


    /**
     * Returns the account ID of the currently authenticated user.
     *
     * @throws SecurityException if there is no authenticated principal in the security context
     */
    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return authentication.getName();
    }
}
