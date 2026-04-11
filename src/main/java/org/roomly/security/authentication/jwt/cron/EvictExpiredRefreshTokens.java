package org.roomly.security.authentication.jwt.cron;

import lombok.RequiredArgsConstructor;
import org.roomly.security.authentication.jwt.repositories.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvictExpiredRefreshTokens {
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Scheduled(cron = "0 0 * * * ?")
    public void evictExpiredTokens () {
        long now = System.currentTimeMillis();
        var expiredTokens = refreshTokenRepository.findAllExpiredAndInactive(now);
        refreshTokenRepository.deleteAll(expiredTokens);
    }
    
}

