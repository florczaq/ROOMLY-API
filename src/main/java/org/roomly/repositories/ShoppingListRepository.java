package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.entities.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, String> {
    List<ShoppingList> findAllByHousehold(Household household);
    
    Optional<ShoppingList> getShoppingListByHouseholdAndOwner (Household household, Profile owner);
}

