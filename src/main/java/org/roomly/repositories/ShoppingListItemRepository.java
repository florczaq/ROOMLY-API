package org.roomly.repositories;

import org.roomly.entities.Product;
import org.roomly.entities.ShoppingList;
import org.roomly.entities.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Integer> {

    Optional<ShoppingListItem> findByShoppingListAndProduct(ShoppingList shoppingList, Product product);
}

