package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.dto.ProductDTO;
import org.roomly.dto.ShoppingListDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.entities.ShoppingList;
import org.roomly.repositories.ShoppingListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    
    /**
     * Gets a shopping list for a household and optional owner.
     * Caller is responsible for fetching the household and determining the owner.
     */
    public ShoppingListDTO getShoppingList(@NotNull Household household, @Nullable Profile owner) {
        ShoppingList shoppingList = shoppingListRepository.getShoppingListByHouseholdAndOwner(household, owner)
          .orElseThrow(() -> new EntityNotFoundException("Shopping list not found"));
        return shoppingList.toDTO();
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
    public ShoppingList createShoppingList (@Nullable Profile profile, @NotNull Household household) {
        return shoppingListRepository.save(new ShoppingList().setOwner(profile));
    }
    
    @Transactional
    public ShoppingList addProductToShoppingList (String householdId, ProductDTO product, Profile addedBy) {
        
        
        return null;
    }
    
}

