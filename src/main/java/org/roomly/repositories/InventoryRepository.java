package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    List<Inventory> findAllByHousehold(Household household);
}

