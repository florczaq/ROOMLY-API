package org.roomly.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.InventoryDTO;

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
    
    /*
     * Each inventory is owned by a single user
     * and a user can own one inventory.
     * If owner is null, inventory is considered
     * shared and can be accessed by all household members.
     */
    @OneToOne(mappedBy = "inventory")
    Profile owner;
    
    public InventoryDTO toDTO () {
        String householdId = owner != null ? owner.getHousehold().getId() : null;
        return new InventoryDTO(
          id,
          householdId
        );
    }
    
}
