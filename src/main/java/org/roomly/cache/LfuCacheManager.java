package org.roomly.cache;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class LfuCacheManager implements CacheManager {
    private final Map<String, Cache> caches = new ConcurrentHashMap<>();
    private final FrequencyTracker<String> tracker;
    private final int maxSize;
    private final long ttlMillis;
    
    @Override
    public Cache getCache (@NonNull String name) {
        return caches.computeIfAbsent(
          name,
          cacheName -> new LfuCache(cacheName, tracker, maxSize, ttlMillis)
        );
    }
    
    @Override
    @NullMarked
    public Collection<String> getCacheNames () {
        return Collections.unmodifiableSet(caches.keySet());
    }
}


