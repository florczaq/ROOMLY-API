package org.roomly.repositories;

import org.roomly.entities.Inventory;
import org.roomly.entities.InventoryItem;
import org.roomly.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Integer> {
    Optional<InventoryItem> findByInventoryAndProduct(Inventory inventory, Product product);
}
