package org.roomly.repositories;

import org.roomly.entities.ShoppingListItem;
import org.roomly.entities.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Integer> {
    
    List<ShoppingListItem> findByShoppingList(ShoppingList shoppingList);
    
    List<ShoppingListItem> findByShoppingListAndPurchasedFalse(ShoppingList shoppingList);
    
    List<ShoppingListItem> findByShoppingListAndPurchasedTrue(ShoppingList shoppingList);
}

