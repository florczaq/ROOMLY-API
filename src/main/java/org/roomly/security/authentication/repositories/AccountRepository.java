package org.roomly.security.authentication.repositories;

import org.roomly.security.authentication.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findFirstById (String id);
    
    Optional<Account> findByDevicesContaining (String deviceId);
    
    Optional<Account> findByEmail (String email);
    
    Optional<Account> findFirstByEmail (String email);
}
