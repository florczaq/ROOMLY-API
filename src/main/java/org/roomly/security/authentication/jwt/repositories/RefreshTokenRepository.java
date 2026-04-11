package org.roomly.security.authentication.jwt.repositories;

import org.roomly.security.authentication.jwt.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findFirstByToken (String token);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.active = false OR rt.expiryDate < :now")
    List<RefreshToken> findAllExpiredAndInactive(@Param("now") long now);
    
}
