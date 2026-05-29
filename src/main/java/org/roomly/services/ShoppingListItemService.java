package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.ShoppingListItemRepository;
import org.springframework.stereotype.Service;

/**
 * Service for operations scoped to individual {@link org.roomly.entities.ShoppingListItem} entities.
 * Item-level mutations (add, remove, count update) are handled by {@link ShoppingListService};
 * this service is reserved for finer-grained item queries or bulk item operations.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListItemService {

    private final ShoppingListItemRepository shoppingListItemRepository;

}

