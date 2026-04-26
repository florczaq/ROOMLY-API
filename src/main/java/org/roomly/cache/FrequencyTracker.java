package org.roomly.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FrequencyTracker<K> {
    private final ConcurrentHashMap<K, AtomicInteger> freq = new ConcurrentHashMap<>();
    
    public void increment (K key) {
        freq.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    public int get (K key) {
        AtomicInteger counter = freq.get(key);
        return counter != null ? counter.get() : 0;
    }
    
    public void remove (K key) {
        freq.remove(key);
    }
    
    public int getAverageFrequency () {
        if (freq.isEmpty()) {
            return 0;
        }
        return (int) freq.values().stream()
            .mapToInt(AtomicInteger::get)
            .average()
            .orElse(0);
    }
    
    public void clear () {
        freq.clear();
    }
}
