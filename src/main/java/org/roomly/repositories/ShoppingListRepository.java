package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, String> {
    List<ShoppingList> findAllByHousehold(Household household);
}

