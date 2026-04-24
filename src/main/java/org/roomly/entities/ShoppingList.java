package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.ShoppingListDTO;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    Household household;
    
    /**
     * Each shopping list is owned by a single user
     * and a user can own one shopping list.
     * If owner is null, shopping list is considered
     * shared and can be accessed by all household members.
     */
    @OneToOne(mappedBy = "shoppingList")
    Profile owner;
    
    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<ShoppingListItem> items = new ArrayList<>();
    
    public ShoppingListDTO toDTO () {
        return new ShoppingListDTO(
          id,
          household.getId(),
          items.stream().map(ShoppingListItem::toDTO).toList()
        );
    }
    
}
