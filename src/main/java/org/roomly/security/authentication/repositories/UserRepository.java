package org.roomly.security.authentication.repositories;

import org.roomly.security.authentication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findFirstById (String id);
    
    
    Optional<User> findByEmail (String email);
}
