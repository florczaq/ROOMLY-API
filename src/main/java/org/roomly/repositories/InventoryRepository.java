package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    
    @Query("SELECT i FROM Inventory i LEFT JOIN i.owner p LEFT JOIN Household h ON (h.sharedInventory = i OR p.household = h) WHERE h = :household")
    List<Inventory> findAllByHousehold(@Param("household") Household household);
}

