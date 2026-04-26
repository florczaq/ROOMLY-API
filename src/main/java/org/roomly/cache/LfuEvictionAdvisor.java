package org.roomly.cache;

import lombok.RequiredArgsConstructor;
import org.ehcache.config.EvictionAdvisor;

@RequiredArgsConstructor
public class LfuEvictionAdvisor<K, V> implements EvictionAdvisor<K, V> {
    private final FrequencyTracker<K> tracker;
    
    @Override
    public boolean adviseAgainstEviction (K key, V value) {
        int freq = tracker.get(key);
        int avgFreq = tracker.getAverageFrequency();
        return freq > avgFreq;
    }
}
