package org.roomly.security.authentication.repositories;

import org.jspecify.annotations.NullMarked;
import org.roomly.security.authentication.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    
    @NullMarked
    Optional<Account> findById (String id);
    
    Optional<Account> findByDevicesContaining (@Param("deviceId") String deviceId);
    
    Optional<Account> findByEmail (String email);
    
    Optional<Account> findFirstByEmail (String email);
}
