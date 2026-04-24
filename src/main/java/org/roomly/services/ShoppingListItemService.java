package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.ShoppingListItemRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ShoppingListItemService {
    
    private final ShoppingListItemRepository shoppingListItemRepository;
    
}

