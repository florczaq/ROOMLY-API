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
@SuppressWarnings("JpaDataSourceORMInspection")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    
    
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    Household household;
    
    /*
     * Each inventory is owned by a single user
     * and a user can own one inventory.
     * If owner is null, inventory is considered
     * shared and can be accessed by all household members.
     */
    @OneToOne(mappedBy = "inventory")
    Profile owner;
    
    public InventoryDTO toDTO () {
        return new InventoryDTO(
          id,
          household.getId()
        );
    }
    
}
