package org.roomly.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.ShoppingListItemDTO;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_list_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class ShoppingListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    @Min(1)
    @Max(1000)
    private int count = 1;
    
    private boolean purchased = false;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
    
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;
    
    private String notes;
    
    @PrePersist
    protected void onCreate () {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
    
    public ShoppingListItemDTO toDTO () {
        return new ShoppingListItemDTO(
          id,
          product.toDTO(),
          count,
          purchased,
          addedAt,
          purchasedAt,
          notes
        );
    }
}

