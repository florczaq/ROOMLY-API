package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.ShoppingListDTO;
import org.roomly.dto.ShoppingListItemDTO;
import org.roomly.entities.Household;
import org.roomly.services.HouseholdService;
import org.roomly.services.ShoppingListService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShoppingListResolver {
    private final ShoppingListService shoppingListService;
    private final HouseholdService householdService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ShoppingListDTO shoppingList(@Argument int id) {
        return shoppingListService.getShoppingList(id).toDTO();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<ShoppingListDTO> allShoppingLists(@Argument String householdId) {
        Household household = householdService.getHousehold(householdId);
        return shoppingListService.getAllShoppingLists(household);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ShoppingListItemDTO addProductToShoppingList(@Argument int productId, @Argument int shoppingListId, @Argument int count, @Argument String notes) {
        return shoppingListService.addProductToShoppingList(productId, shoppingListId, count, notes).toDTO();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ShoppingListItemDTO removeProductFromShoppingList(@Argument int productId, @Argument int shoppingListId, @Argument int count, @Argument String notes) {
        return shoppingListService.removeProductFromShoppingList(productId, shoppingListId, count, notes).toDTO();
    }
}

