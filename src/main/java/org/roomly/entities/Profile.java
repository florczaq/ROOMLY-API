package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.AvatarDTO;
import org.roomly.dto.ProfileDTO;
import org.roomly.security.authentication.entities.Account;
import org.roomly.services.ColorsService;

@Entity
@Table(name = "users_profiles")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class Profile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    String id;
    
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;
    
    String avatarName;
    String avatarColorName;
    
    String nickname;
    
    @ManyToOne
    @JoinColumn(name = "household_id")
    Household household;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_id")
    Inventory inventory;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shopping_list_id")
    ShoppingList shoppingList;
    
    @Override
    public String toString () {
        return """
               \nUser {
                    id: %s,
                    account: %s,
                    avatarName: %s,
                    avatarColorName: %s,
                    nickname: %s,
                    household: %s
               """.formatted(id, account.getId(), avatarName, avatarColorName, nickname, household.getName());
    }
    
    public ProfileDTO toDTO () {
        return new ProfileDTO(
          id,
          nickname,
          new AvatarDTO(avatarName, avatarColorName, ColorsService.getHexByColor(avatarColorName)),
          inventory != null ? inventory.toDTO() : null,
          shoppingList != null ? shoppingList.toDTO() : null
        );
    }
}
