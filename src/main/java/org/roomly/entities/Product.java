package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.enums.QuantityUnits;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Product {
    @Id
    int id;
    
    @Column(nullable = false)
    String name;
    
    @Column(nullable = false)
    String brand;
    
    int quantity;
    
    @Enumerated(EnumType.STRING)
    QuantityUnits unit;
    
    @Column(unique = true, nullable = false)
    String barcode;
    
}
