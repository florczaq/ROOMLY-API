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

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ProductsRepository productsRepository;
    private final ProfileRepository profileRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    /**
     * Gets a shopping list for a household and optional owner.
     * Caller is responsible for fetching the household and determining the owner.
     */
    public ShoppingList getShoppingList(int id) {
        return shoppingListRepository.getShoppingListById(id)
            .orElseThrow(() -> new EntityNotFoundException("Shopping list not found"));
    }

    /**
     * Gets all shopping lists for a household.
     * Caller is responsible for fetching the household.
     */
    public List<ShoppingListDTO> getAllShoppingLists(@NotNull Household household) {
        List<ShoppingList> shoppingLists = shoppingListRepository.findAllByHousehold(household);
        return shoppingLists.stream().map(ShoppingList::toDTO).toList();
    }

    @Transactional
    public ShoppingList createShoppingList(@Nullable Profile profile, @NotNull Household household) {
        return shoppingListRepository.save(new ShoppingList().setOwner(profile));
    }

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


    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return authentication.getName();
    }
}
