package org.roomly.config;

import org.roomly.cache.FrequencyTracker;
import org.roomly.cache.LfuCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig {
    
    @Bean
    public FrequencyTracker<String> frequencyTracker () {
        return new FrequencyTracker<>();
    }
    
    @Bean
    public CacheManager cacheManager (FrequencyTracker<String> tracker) {
        int maxSize = 500;
        long ttlMillis = Duration.ofHours(12).toMillis();
        return new LfuCacheManager(tracker, maxSize, ttlMillis);
    }
}
