package org.roomly.cache;

import lombok.RequiredArgsConstructor;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;

@RequiredArgsConstructor
public class LfuEventListener implements CacheEventListener<String, Object> {
    private final FrequencyTracker<String> tracker;
    
    @Override
    public void onEvent (CacheEvent<? extends String, ?> event) {
        String key = event.getKey();
        EventType type = event.getType();
        
        if (type == EventType.CREATED || type == EventType.UPDATED) {
            tracker.increment(key);
        }
        
        if (type == EventType.REMOVED || type == EventType.EXPIRED || type == EventType.EVICTED) {
            tracker.remove(key);
        }
    }
}
