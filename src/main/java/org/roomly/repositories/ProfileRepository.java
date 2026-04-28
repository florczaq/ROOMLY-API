package org.roomly.repositories;

import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.security.authentication.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    
    boolean existsByAccountAndHousehold (Account account, Household household);
    
    boolean existsByHouseholdAndNickname (Household household, String nickname);
    
    double countByHousehold (Household household);
    
    @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.household = :household AND (p.avatarName = :avatarName OR p.avatarColorName = :avatarColorName)")
    boolean existsByHouseholdAndAvatarNameOrAvatarColorName (Household household,
      String avatarName,
      String avatarColorName
    );
    
    List<Profile> findAllByHouseholdId (String householdId);
    
    Optional<Profile> findByHouseholdAndAccount (Household household, Account account);
    
    Optional<Profile> findProfileById (String id);
    
    @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.id = :profileId AND p.account.id = :accountId")
    boolean existsByIdAndAccountId (String profileId, String accountId);
}
