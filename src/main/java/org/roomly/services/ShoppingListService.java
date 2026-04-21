package org.roomly.services;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.entities.ShoppingList;
import org.roomly.repositories.ShoppingListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    
    public String getShoppingList () {
        return "ShoppingListService";
    }
    
    @Transactional
    public int createShoppingList (@Nullable Profile user, @NotNull Household household) {
        ShoppingList savedShoppingList =
          shoppingListRepository.save(
            new ShoppingList()
              .setHousehold(household)
              .setOwner(user)
          );
        return savedShoppingList.getId();
    }
    
    public String updateShoppingList () {
        return "ShoppingListService";
    }
    
    public String deleteShoppingList () {
        return "ShoppingListService";
    }
    
}

