package org.roomly.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.InventoryDTO;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    
    /**
     * Each inventory is owned by a single user
     * and a user can own one inventory.
     * If owner is null, inventory is considered
     * shared and can be accessed by all household members.
     */
    @OneToOne(mappedBy = "inventory")
    Profile owner;
    
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<InventoryItem> items = new ArrayList<>();
    
    public InventoryDTO toDTO () {
        return new InventoryDTO(
          id,
          items.stream().map(InventoryItem::toDTO).toList()
        );
    }
    
}
