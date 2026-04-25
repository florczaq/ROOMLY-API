package org.roomly.repositories;

import org.roomly.entities.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, String> {
    boolean existsByJoinCode (String joinCode);
    
    Optional<Household> findByJoinCode (String joinCode);
    
    @Query("SELECT h FROM Household h JOIN h.members m WHERE m.account.id = :accountId")
    List<Household> findAllByAccount (@Param("accountId") String accountId);
}
