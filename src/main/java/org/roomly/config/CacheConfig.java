package org.roomly.config;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.roomly.cache.FrequencyTracker;
import org.roomly.cache.LfuEventListener;
import org.roomly.cache.LfuEvictionAdvisor;
import org.roomly.logs.cache.CacheEventLogger;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig {
    
    @Bean
    public FrequencyTracker<String> frequencyTracker() {
        return new FrequencyTracker<>();
    }
    
    @Bean
    public CacheManager ehCacheManager (FrequencyTracker<String> tracker) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        
        CacheConfiguration<String, Object> cacheConfiguration =
          CacheConfigurationBuilder
            .newCacheConfigurationBuilder(
              String.class,
              Object.class,
              ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(500, EntryUnit.ENTRIES)
            )
            .withEvictionAdvisor(new LfuEvictionAdvisor<>(tracker))
            // Event listener for frequency tracking (LFU implementation)
            .withService(CacheEventListenerConfigurationBuilder
              .newEventListenerConfiguration(
                new LfuEventListener(tracker),
                EventType.CREATED,
                EventType.UPDATED,
                EventType.REMOVED,
                EventType.EXPIRED,
                EventType.EVICTED
              )
              .unordered()
              .asynchronous()
            )
            // Event listener for logging
            .withService(CacheEventListenerConfigurationBuilder
              .newEventListenerConfiguration(
                CacheEventLogger.class,
                EventType.CREATED,
                EventType.REMOVED,
                EventType.EXPIRED
              )
              .unordered()
              .asynchronous()
            )
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(12)))
            .build();
        
        javax.cache.configuration.Configuration<String, Object> jcacheConfig =
          Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration);
        
        cacheManager.createCache("products", jcacheConfig);
        
        return cacheManager;
    }
}
//TODO test cache eviction and logging behavior
