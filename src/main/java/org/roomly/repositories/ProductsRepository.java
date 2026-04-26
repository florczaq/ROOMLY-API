package org.roomly.repositories;

import org.roomly.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Integer> {
    
    Optional<Product> findByBarcode (String barcode);
}

