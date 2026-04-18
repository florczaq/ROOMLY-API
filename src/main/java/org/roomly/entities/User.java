package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.security.authentication.entities.Account;

@Entity
@Table(name = "users_profiles")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class User {
    
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
    @JoinColumn(name = "household_id", nullable = false)
    Household household;
    
    @Override
    public String toString () {
        return """
               \nUser {
                    id: %s,
                    account: %s,
                    avatar: %s,
                    nickname: %s,
                    household: %s
               """.formatted(id, account.getEmail(), avatarColorName, nickname, household.getName());
    }
    
}
