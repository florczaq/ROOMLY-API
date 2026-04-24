package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.ShoppingListDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.services.HouseholdService;
import org.roomly.services.ProfileService;
import org.roomly.services.ShoppingListService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShoppingListResolver {
    private final ShoppingListService shoppingListService;
    private final HouseholdService householdService;
    private final ProfileService profileService;
    
    @QueryMapping
    public ShoppingListDTO shoppingList (@Argument String householdId, @Argument Boolean shared) {
        Household household = householdService.getHousehold(householdId);
        Profile owner = (shared != null && shared)
          ? null
          : profileService.getCurrentlyAuthenticatedUserProfile(household);
        return shoppingListService.getShoppingList(household, owner);
    }
    
    @QueryMapping
    public List<ShoppingListDTO> allShoppingLists (@Argument String householdId) {
        Household household = householdService.getHousehold(householdId);
        return shoppingListService.getAllShoppingLists(household);
    }
}

