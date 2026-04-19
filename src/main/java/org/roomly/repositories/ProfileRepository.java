package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.security.authentication.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    
    boolean existsByAccountAndHousehold (Account account, Household household);
    
    boolean existsByHouseholdAndNickname (Household household, String nickname);
    
    double countByHousehold (Household household);
    
    boolean existsByHouseholdAndAvatarNameAndAvatarColorName (Household household, String avatarName, String avatarColorName);
    
    List<Profile> findAllByHouseholdId (String householdId);
}
