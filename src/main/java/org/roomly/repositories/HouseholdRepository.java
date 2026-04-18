package org.roomly.repositories;

import org.roomly.entities.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, String> {
    boolean existsByJoinCode (String joinCode);
    
    Optional<Household> findByJoinCode (String joinCode);
}
