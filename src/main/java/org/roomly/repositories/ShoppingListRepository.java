package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.entities.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, String> {
    
    @Query("SELECT s FROM ShoppingList s LEFT JOIN s.owner p LEFT JOIN Household h ON (h.sharedShoppingList = s OR p.household = h) WHERE h = :household")
    List<ShoppingList> findAllByHousehold(@Param("household") Household household);
    
    @Query("SELECT s FROM ShoppingList s LEFT JOIN s.owner p LEFT JOIN Household h ON (h.sharedShoppingList = s OR p.household = h) WHERE h = :household AND (s.owner = :owner OR (:owner IS NULL AND s.owner IS NULL))")
    Optional<ShoppingList> getShoppingListByHouseholdAndOwner(@Param("household") Household household, @Param("owner") Profile owner);
}

