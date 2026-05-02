package org.roomly.cache;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LfuCache implements org.springframework.cache.Cache {
    private final String name;
    private final ConcurrentHashMap<Object, Object> cache;
    private final FrequencyTracker<String> tracker;
    private final int maxSize;
    private final long ttlMillis;
    private final ConcurrentHashMap<Object, Long> timestamps;
    
    public LfuCache (String name, FrequencyTracker<String> tracker, int maxSize, long ttlMillis) {
        this.name = name;
        this.cache = new ConcurrentHashMap<>();
        this.tracker = tracker;
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
        this.timestamps = new ConcurrentHashMap<>();
    }
    
    @Override
    @NonNull
    public String getName () {
        return name;
    }
    
    @Override
    @NonNull
    public Object getNativeCache () {
        return cache;
    }
    
    @Override
    @Nullable
    public ValueWrapper get (@NonNull Object key) {
        evictExpired();
        Object value = cache.get(key);
        if (value != null) {
            tracker.increment(String.valueOf(key));
            return new SimpleValueWrapper(value);
        }
        return null;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get (@NonNull Object key, @Nullable Class<T> type) {
        evictExpired();
        Object value = cache.get(key);
        if (value != null) {
            tracker.increment(String.valueOf(key));
            if (type != null && !type.isInstance(value)) {
                throw new IllegalStateException(
                  "Cached value is not of required type [" + type.getName() + "]: " + value);
            }
            return type != null ? type.cast(value) : (T) value;
        }
        return null;
    }
    
    @Override
    @Nullable
    public <T> T get (@NonNull Object key, @NonNull Callable<T> valueLoader) {
        evictExpired();
        Object value = cache.get(key);
        if (value != null) {
            tracker.increment(String.valueOf(key));
            //noinspection unchecked
            return (T) value;
        }
        
        try {
            T loadedValue = valueLoader.call();
            put(key, loadedValue);
            return loadedValue;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }
    
    @Override
    public void put (@NonNull Object key, @Nullable Object value) {
        evictExpired();
        
        // Check if we need to evict
        if (cache.size() >= maxSize && !cache.containsKey(key)) {
            evictLfu();
        }
        
        cache.put(key, value);
        timestamps.put(key, System.currentTimeMillis());
        log.debug("Cache PUT: {} - Key: {}", name, key);
    }
    
    @Override
    @Nullable
    public ValueWrapper putIfAbsent (@NonNull Object key, @Nullable Object value) {
        evictExpired();
        Object existing = cache.putIfAbsent(key, value);
        if (existing == null) {
            timestamps.put(key, System.currentTimeMillis());
            log.debug("Cache PUT_IF_ABSENT: {} - Key: {}", name, key);
            return null;
        }
        return new SimpleValueWrapper(existing);
    }
    
    @Override
    public void evict (@NonNull Object key) {
        cache.remove(key);
        timestamps.remove(key);
        tracker.remove(String.valueOf(key));
        log.debug("Cache EVICT: {} - Key: {}", name, key);
    }
    
    @Override
    public boolean evictIfPresent (@NonNull Object key) {
        boolean removed = cache.remove(key) != null;
        if (removed) {
            timestamps.remove(key);
            tracker.remove(String.valueOf(key));
            log.debug("Cache EVICT_IF_PRESENT: {} - Key: {}", name, key);
        }
        return removed;
    }
    
    @Override
    public void clear () {
        cache.clear();
        timestamps.clear();
        tracker.clear();
        log.info("Cache CLEARED: {}", name);
    }
    
    @Override
    public boolean invalidate () {
        if (!cache.isEmpty()) {
            clear();
            return true;
        }
        return false;
    }
    
    /**
     * Evict the least frequently used item from the cache
     */
    private void evictLfu () {
        if (cache.isEmpty()) {
            return;
        }
        
        // Find the key with the lowest frequency
        String lfuKey = null;
        int minFreq = Integer.MAX_VALUE;
        
        for (Object key : cache.keySet()) {
            String keyStr = String.valueOf(key);
            int freq = tracker.get(keyStr);
            
            if (freq < minFreq) {
                minFreq = freq;
                lfuKey = keyStr;
            }
        }
        
        if (lfuKey != null) {
            log.info("Cache LFU EVICTION: {} - Key: {} (frequency: {})", name, lfuKey, minFreq);
            evict(lfuKey);
        }
    }
    
    /**
     * Remove expired entries based on TTL
     */
    private void evictExpired () {
        long now = System.currentTimeMillis();
        timestamps.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > ttlMillis) {
                Object key = entry.getKey();
                cache.remove(key);
                tracker.remove(String.valueOf(key));
                log.debug("Cache EXPIRED: {} - Key: {}", name, key);
                return true;
            }
            return false;
        });
    }
}


