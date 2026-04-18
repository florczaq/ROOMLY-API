package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.User;
import org.roomly.security.authentication.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    
    boolean existsByAccountAndHousehold (Account account, Household household);
    
    boolean existsByHouseholdAndNickname (Household household, String nickname);
    
    double countByHousehold (Household household);
    
    boolean existsByHouseholdAndAvatarNameAndAvatarColorName (Household household, String avatarName, String avatarColorName);
}
