package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.ProductDTO;
import org.roomly.enums.ProductInfoSource;

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
    
    String quantity;
    
    //    @Enumerated(EnumType.STRING)
    //    QuantityUnits unit;
    
    @Column(unique = true)
    String barcode;
    
    @Enumerated(EnumType.STRING)
    ProductInfoSource infoSource;
    
    public ProductDTO toDTO () {
        return new ProductDTO(
          id,
          barcode,
          name,
          brand,
          quantity
        );
    }
}
