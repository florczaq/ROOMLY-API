package org.roomly.logs.cache;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEventListener;

@Slf4j
public class CacheEventLogger implements CacheEventListener<String, Object> {
    @Override
    public void onEvent (org.ehcache.event.CacheEvent<? extends String, ?> cacheEvent) {
        log.info(
          "Cache event: {} - Key: {}, Old Value: {}, New Value: {}",
          cacheEvent.getType(),
          cacheEvent.getKey(),
          cacheEvent.getOldValue(),
          cacheEvent.getNewValue()
        );
    }
}
