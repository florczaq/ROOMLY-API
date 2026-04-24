package org.roomly.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.InventoryItemDTO;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    @Min(1)
    @Max(1000)
    private int count = 1;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "added_by_profile_id")
    private Profile addedBy;
    
    private String notes;
    
    @PrePersist
    protected void onCreate () {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
    
    public InventoryItemDTO toDTO () {
        return new InventoryItemDTO(
          id,
          product.toDTO(),
          count,
          addedAt,
          notes
        );
    }
}

