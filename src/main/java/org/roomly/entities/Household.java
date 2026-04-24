package org.roomly.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.HouseholdDTO;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class Household {
    @Id
    String id;
    
    String name;
    
    @Column(unique = true, nullable = false, length = 6)
    String joinCode;
    
    @Min(1)
    @Max(30)
    @Column(nullable = false)
    int membersLimit;
    
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    Profile owner;
    
    @OneToMany(mappedBy = "household", fetch = FetchType.LAZY)
    List<Profile> members = new ArrayList<>();
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shared_inventory_id")
    Inventory sharedInventory;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shared_shopping_list_id")
    ShoppingList sharedShoppingList;
    
    @Override
    public String toString () {
        return """
               \nHousehold {
                    id: %s,
                    name: %s,
                    joinCode: %s,
                    membersLimit: %d,
                    ownerId: %s
               """.formatted(id, name, joinCode, membersLimit, owner.getId());
    }
    
    public HouseholdDTO toDTO () {
        return new HouseholdDTO(
          id,
          name,
          joinCode,
          membersLimit,
          sharedInventory != null ? sharedInventory.toDTO() : null,
          sharedShoppingList != null ? sharedShoppingList.toDTO() : null
        );
    }
    
}
