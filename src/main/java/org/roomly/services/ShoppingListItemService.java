package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.entities.Product;
import org.roomly.entities.ShoppingList;
import org.roomly.entities.ShoppingListItem;
import org.roomly.entities.User;
import org.roomly.repositories.ShoppingListItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListItemService {
    
    private final ShoppingListItemRepository shoppingListItemRepository;
    
    @Transactional
    public ShoppingListItem addProductToShoppingList (
      ShoppingList shoppingList,
      Product product,
      int quantity,
      User addedBy,
      String notes
    ) {
        ShoppingListItem item = new ShoppingListItem()
          .setShoppingList(shoppingList)
          .setProduct(product)
          .setQuantity(quantity)
          .setAddedBy(addedBy)
          .setNotes(notes)
          .setPurchased(false);
        
        return shoppingListItemRepository.save(item);
    }
    
    @Transactional
    public ShoppingListItem markAsPurchased (int itemId) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId)
          .orElseThrow(() -> new RuntimeException("Item not found"));
        
        item.setPurchased(true);
        item.setPurchasedAt(LocalDateTime.now());
        
        return shoppingListItemRepository.save(item);
    }
    
    public List<ShoppingListItem> getItemsFromShoppingList (ShoppingList shoppingList) {
        return shoppingListItemRepository.findByShoppingList(shoppingList);
    }
    
    public List<ShoppingListItem> getUnpurchasedItems (ShoppingList shoppingList) {
        return shoppingListItemRepository.findByShoppingListAndPurchasedFalse(shoppingList);
    }
}

