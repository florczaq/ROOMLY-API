package org.roomly.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyTrackerTest {
    
    private FrequencyTracker<String> tracker;
    
    @BeforeEach
    void setUp() {
        tracker = new FrequencyTracker<>();
    }
    
    @Test
    void testInitialFrequencyIsZero() {
        assertEquals(0, tracker.get("non-existent-key"),
                "Frequency should be 0 for non-existent key");
    }
    
    @Test
    void testIncrementSingleKey() {
        String key = "test-key";
        
        tracker.increment(key);
        assertEquals(1, tracker.get(key), "Frequency should be 1 after one increment");
        
        tracker.increment(key);
        assertEquals(2, tracker.get(key), "Frequency should be 2 after two increments");
        
        tracker.increment(key);
        tracker.increment(key);
        tracker.increment(key);
        assertEquals(5, tracker.get(key), "Frequency should be 5 after five increments");
    }
    
    @Test
    void testIncrementMultipleKeys() {
        tracker.increment("key1");
        tracker.increment("key2");
        tracker.increment("key1");
        tracker.increment("key3");
        tracker.increment("key2");
        tracker.increment("key1");
        
        assertEquals(3, tracker.get("key1"), "key1 frequency should be 3");
        assertEquals(2, tracker.get("key2"), "key2 frequency should be 2");
        assertEquals(1, tracker.get("key3"), "key3 frequency should be 1");
    }
    
    @Test
    void testRemoveKey() {
        String key = "remove-test";
        
        tracker.increment(key);
        tracker.increment(key);
        assertEquals(2, tracker.get(key), "Frequency should be 2 before remove");
        
        tracker.remove(key);
        assertEquals(0, tracker.get(key), "Frequency should be 0 after remove");
    }
    
    @Test
    void testRemoveNonExistentKey() {
        // Should not throw exception
        assertDoesNotThrow(() -> tracker.remove("non-existent-key"),
                "Removing non-existent key should not throw exception");
    }
    
    @Test
    void testAverageFrequencyEmptyTracker() {
        assertEquals(0, tracker.getAverageFrequency(),
                "Average frequency of empty tracker should be 0");
    }
    
    @Test
    void testAverageFrequencySingleKey() {
        tracker.increment("key1");
        tracker.increment("key1");
        tracker.increment("key1");
        
        assertEquals(3, tracker.getAverageFrequency(),
                "Average frequency with single key should equal its frequency");
    }
    
    @Test
    void testAverageFrequencyMultipleKeys() {
        // Frequencies: 2, 4, 6 -> Average = 4
        tracker.increment("key1");
        tracker.increment("key1");
        
        tracker.increment("key2");
        tracker.increment("key2");
        tracker.increment("key2");
        tracker.increment("key2");
        
        tracker.increment("key3");
        tracker.increment("key3");
        tracker.increment("key3");
        tracker.increment("key3");
        tracker.increment("key3");
        tracker.increment("key3");
        
        int average = tracker.getAverageFrequency();
        assertEquals(4, average, "Average of [2, 4, 6] should be 4");
    }
    
    @Test
    void testAverageFrequencyWithRemoval() {
        tracker.increment("key1"); // 1
        tracker.increment("key2"); // 1
        tracker.increment("key2"); // 2
        tracker.increment("key3"); // 1
        tracker.increment("key3"); // 2
        tracker.increment("key3"); // 3
        
        // Average: (1 + 2 + 3) / 3 = 2
        assertEquals(2, tracker.getAverageFrequency(), "Average should be 2");
        
        tracker.remove("key3");
        
        // Average: (1 + 2) / 2 = 1
        assertEquals(1, tracker.getAverageFrequency(),
                "Average should be 1 after removing highest frequency key");
    }
    
    @Test
    void testClear() {
        tracker.increment("key1");
        tracker.increment("key2");
        tracker.increment("key3");
        
        assertTrue(tracker.get("key1") > 0, "Tracker should have data before clear");
        
        tracker.clear();
        
        assertEquals(0, tracker.get("key1"), "key1 frequency should be 0 after clear");
        assertEquals(0, tracker.get("key2"), "key2 frequency should be 0 after clear");
        assertEquals(0, tracker.get("key3"), "key3 frequency should be 0 after clear");
        assertEquals(0, tracker.getAverageFrequency(),
                "Average frequency should be 0 after clear");
    }
    
    @Test
    void testThreadSafety() throws InterruptedException {
        String key = "concurrent-key";
        int threadCount = 10;
        int incrementsPerThread = 100;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        tracker.increment(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        int expectedFrequency = threadCount * incrementsPerThread;
        assertEquals(expectedFrequency, tracker.get(key),
                "Frequency should be " + expectedFrequency + " after concurrent increments");
    }
    
    @Test
    void testConcurrentMultipleKeys() throws InterruptedException {
        int threadCount = 5;
        int keysPerThread = 10;
        int incrementsPerKey = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int k = 0; k < keysPerThread; k++) {
                        String key = "thread" + threadId + "-key" + k;
                        for (int i = 0; i < incrementsPerKey; i++) {
                            tracker.increment(key);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Verify each key has the expected frequency
        for (int t = 0; t < threadCount; t++) {
            for (int k = 0; k < keysPerThread; k++) {
                String key = "thread" + t + "-key" + k;
                assertEquals(incrementsPerKey, tracker.get(key),
                        "Key " + key + " should have frequency " + incrementsPerKey);
            }
        }
    }
    
    @Test
    void testAverageCalculationWithLargeNumbers() {
        // Test with larger frequencies
        for (int i = 0; i < 100; i++) {
            String key = "key" + i;
            for (int j = 0; j <= i; j++) {
                tracker.increment(key);
            }
        }
        
        // Frequencies: 1, 2, 3, ..., 100
        // Average: (1+2+...+100)/100 = (100*101/2)/100 = 50.5 -> 50 (integer division)
        int average = tracker.getAverageFrequency();
        assertTrue(average >= 49 && average <= 51,
                "Average should be around 50, got: " + average);
    }
    
    @Test
    void testIncrementAfterRemove() {
        String key = "test-key";
        
        tracker.increment(key);
        tracker.increment(key);
        assertEquals(2, tracker.get(key), "Frequency should be 2");
        
        tracker.remove(key);
        assertEquals(0, tracker.get(key), "Frequency should be 0 after remove");
        
        tracker.increment(key);
        assertEquals(1, tracker.get(key), "Frequency should be 1 after increment post-remove");
    }
}

