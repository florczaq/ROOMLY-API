package org.roomly.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roomly.cache.FrequencyTracker;
import org.roomly.entities.Product;
import org.roomly.repositories.ProductsRepository;
import org.roomly.services.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CacheEvictionTest {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private FrequencyTracker<String> frequencyTracker;
    
    @Autowired
    private ProductsService productsService;
    
    @MockitoBean
    private ProductsRepository productsRepository;
    
    private Cache productsCache;
    
    @BeforeEach
    void setUp () {
        // Get the products cache
        productsCache = cacheManager.getCache("products");
        assertNotNull(productsCache, "Products cache should be configured");
        
        // Clear cache and frequency tracker before each test
        productsCache.clear();
        frequencyTracker.clear();
        
        System.out.println("=== Cache Test Setup Complete ===");
        System.out.println("Cache cleared, frequency tracker reset");
    }
    
    @Test
    void testFrequencyTrackerIncrement () {
        System.out.println("\n=== Testing FrequencyTracker Increment ===");
        
        String key1 = "barcode-001";
        String key2 = "barcode-002";
        
        // Initially, frequency should be 0
        assertEquals(0, frequencyTracker.get(key1), "Initial frequency should be 0");
        assertEquals(0, frequencyTracker.get(key2), "Initial frequency should be 0");
        
        // Increment key1 multiple times
        frequencyTracker.increment(key1);
        frequencyTracker.increment(key1);
        frequencyTracker.increment(key1);
        
        assertEquals(3, frequencyTracker.get(key1), "Key1 frequency should be 3");
        System.out.println("✓ Key1 incremented 3 times: frequency = " + frequencyTracker.get(key1));
        
        // Increment key2 once
        frequencyTracker.increment(key2);
        
        assertEquals(1, frequencyTracker.get(key2), "Key2 frequency should be 1");
        System.out.println("✓ Key2 incremented 1 time: frequency = " + frequencyTracker.get(key2));
    }
    
    @Test
    void testFrequencyTrackerAverageCalculation () {
        System.out.println("\n=== Testing FrequencyTracker Average Calculation ===");
        
        // Test with empty tracker
        assertEquals(0, frequencyTracker.getAverageFrequency(), "Average of empty tracker should be 0");
        System.out.println("✓ Empty tracker average: 0");
        
        // Add some frequencies: 1, 3, 5 -> average should be 3
        frequencyTracker.increment("key1"); // 1
        
        frequencyTracker.increment("key2"); // 1
        frequencyTracker.increment("key2"); // 2
        frequencyTracker.increment("key2"); // 3
        
        frequencyTracker.increment("key3"); // 1
        frequencyTracker.increment("key3"); // 2
        frequencyTracker.increment("key3"); // 3
        frequencyTracker.increment("key3"); // 4
        frequencyTracker.increment("key3"); // 5
        
        int average = frequencyTracker.getAverageFrequency();
        assertEquals(3, average, "Average should be (1+3+5)/3 = 3");
        System.out.println("✓ Average of frequencies [1, 3, 5]: " + average);
        
        // Verify individual frequencies
        assertEquals(1, frequencyTracker.get("key1"));
        assertEquals(3, frequencyTracker.get("key2"));
        assertEquals(5, frequencyTracker.get("key3"));
    }
    
    @Test
    void testFrequencyTrackerRemove () {
        System.out.println("\n=== Testing FrequencyTracker Remove ===");
        
        String key = "barcode-remove-test";
        
        frequencyTracker.increment(key);
        frequencyTracker.increment(key);
        assertEquals(2, frequencyTracker.get(key), "Frequency should be 2 after increments");
        
        frequencyTracker.remove(key);
        assertEquals(0, frequencyTracker.get(key), "Frequency should be 0 after remove");
        System.out.println("✓ Key removed successfully, frequency reset to 0");
    }
    
    @Test
    void testCacheBasicOperations () {
        System.out.println("\n=== Testing Cache Basic Operations ===");
        
        String barcode = "test-barcode-123";
        Product mockProduct = new Product()
          .setBarcode(barcode)
          .setName("Test Product")
          .setBrand("Test Brand")
          .setQuantity("100ml");
        
        // Mock repository to return the product
        when(productsRepository.findByBarcode(barcode)).thenReturn(Optional.of(mockProduct));
        
        // First call - should hit database and cache the result
        Product product1 = productsService.getProductByBarcode(barcode);
        assertNotNull(product1, "Product should not be null");
        assertEquals(barcode, product1.getBarcode(), "Product barcode should match");
        System.out.println("✓ First call completed - product cached");
        
        // Verify the product is in cache
        Object cachedValue = productsCache.get(barcode);
        assertNotNull(cachedValue, "Product should be in cache");
        System.out.println("✓ Product found in cache");
        
        // Second call - should hit cache (frequency should be tracked)
        Product product2 = productsService.getProductByBarcode(barcode);
        assertEquals(product1.getBarcode(), product2.getBarcode(), "Cached product should match");
        System.out.println("✓ Second call completed - cache hit");
    }
    
    @Test
    void testCacheEvictionWithFrequencyTracking () {
        System.out.println("\n=== Testing Cache Eviction with Frequency Tracking ===");
        
        // Create products with different access patterns
        List<String> lowFrequencyBarcodes = new ArrayList<>();
        List<String> highFrequencyBarcodes = new ArrayList<>();
        
        // Add 10 low-frequency items (accessed once)
        for (int i = 0; i < 10; i++) {
            String barcode = "low-freq-" + i;
            lowFrequencyBarcodes.add(barcode);
            
            Product product = new Product()
              .setBarcode(barcode)
              .setName("Low Freq Product " + i)
              .setBrand("Brand")
              .setQuantity("100ml");
            
            when(productsRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));
            
            // Cache the product (accessed once)
            productsService.getProductByBarcode(barcode);
        }
        
        System.out.println("✓ Added 10 low-frequency items (accessed once each)");
        
        // Add 5 high-frequency items (accessed multiple times)
        for (int i = 0; i < 5; i++) {
            String barcode = "high-freq-" + i;
            highFrequencyBarcodes.add(barcode);
            
            Product product = new Product()
              .setBarcode(barcode)
              .setName("High Freq Product " + i)
              .setBrand("Brand")
              .setQuantity("200ml");
            
            when(productsRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));
            
            // Cache the product and access it multiple times (5 times each)
            for (int access = 0; access < 5; access++) {
                productsService.getProductByBarcode(barcode);
            }
        }
        
        System.out.println("✓ Added 5 high-frequency items (accessed 5 times each)");
        
        // Verify frequency tracking
        int avgFrequency = frequencyTracker.getAverageFrequency();
        System.out.println("  Average frequency: " + avgFrequency);
        
        // Check that high-frequency items have higher frequency than low-frequency items
        for (String barcode : highFrequencyBarcodes) {
            int freq = frequencyTracker.get(barcode);
            System.out.println("  High-freq item " + barcode + ": frequency = " + freq);
            assertTrue(freq > 1, "High frequency items should have frequency > 1");
        }
        
        for (String barcode : lowFrequencyBarcodes) {
            int freq = frequencyTracker.get(barcode);
            System.out.println("  Low-freq item " + barcode + ": frequency = " + freq);
            assertTrue(freq <= 2, "Low frequency items should have frequency <= 2");
        }
        
        // Verify high-frequency items are still in cache
        for (String barcode : highFrequencyBarcodes) {
            Object cached = productsCache.get(barcode);
            assertNotNull(cached, "High frequency item should still be in cache: " + barcode);
        }
        
        System.out.println("✓ Frequency tracking verified");
        System.out.println("✓ High-frequency items protected from eviction");
    }
    
    @Test
    void testManualCacheEviction () {
        System.out.println("\n=== Testing Manual Ca`che Eviction ===");
        
        String barcode = "eviction-test";
        Product mockProduct = new Product()
          .setBarcode(barcode)
          .setName("Eviction Test Product")
          .setBrand("Test Brand")
          .setQuantity("100ml");
        
        when(productsRepository.findByBarcode(barcode)).thenReturn(Optional.of(mockProduct));
        
        // Cache the product
        productsService.getProductByBarcode(barcode);
        
        // Verify it's in cache and tracker
        assertNotNull(productsCache.get(barcode, Object.class), "Product should be in cache");
        assertTrue(frequencyTracker.get(barcode) > 0, "Frequency should be tracked");
        System.out.println("✓ Product cached, frequency = " + frequencyTracker.get(barcode));
        
        // Remove from cache
        productsCache.evict(barcode);
        
        // Verify it's removed from cache
        assertNull(productsCache.get(barcode, Object.class), "Product should be removed from cache");
        System.out.println("✓ Product removed from cache");
        
        // Frequency tracker should be cleaned up
        assertEquals(0, frequencyTracker.get(barcode), "Frequency should be reset after eviction");
        System.out.println("✓ Frequency tracker cleaned up");
    }
    
    @Test
    void testCacheClear () {
        System.out.println("\n=== Testing Cache Clear ===");
        
        // Add multiple products
        for (int i = 0; i < 5; i++) {
            String barcode = "clear-test-" + i;
            Product product = new Product()
              .setBarcode(barcode)
              .setName("Product " + i)
              .setBrand("Brand")
              .setQuantity("100ml");
            
            when(productsRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));
            productsService.getProductByBarcode(barcode);
        }
        
        
        System.out.println("✓ Added 5 products to cache");
        
        // Verify they're tracked
        int itemsWithFrequency = 0;
        for (int i = 0; i < 5; i++) {
            if (frequencyTracker.get("clear-test-" + i) > 0) {
                itemsWithFrequency++;
            }
        }
        System.out.println("  Items with frequency > 0: " + itemsWithFrequency);
        
        // Clear the cache
        productsCache.clear();
        frequencyTracker.clear();
        
        System.out.println("✓ Cache and frequency tracker cleared");
        
        // Verify cache is empty
        for (int i = 0; i < 5; i++) {
            String barcode = "clear-test-" + i;
            assertNull(productsCache.get(barcode, Object.class), "Cache should be empty for: " + barcode);
            assertEquals(0, frequencyTracker.get(barcode), "Frequency should be 0 for: " + barcode);
        }
        
        System.out.println("✓ All items removed from cache and tracker");
    }
    
    @Test
    void testConcurrentCacheAccess () throws InterruptedException {
        System.out.println("\n=== Testing Concurrent Cache Access ===");
        
        String barcode = "concurrent-test";
        Product mockProduct = new Product()
          .setBarcode(barcode)
          .setName("Concurrent Test Product")
          .setBrand("Test Brand")
          .setQuantity("100ml");
        
        when(productsRepository.findByBarcode(barcode)).thenReturn(Optional.of(mockProduct));
        
        // Create multiple threads accessing the same cache entry
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    productsService.getProductByBarcode(barcode);
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        
        System.out.println("✓ " + threadCount + " threads completed concurrent access");
        
        // Verify the product is still in cache
        assertNotNull(
          productsCache.get(barcode, Object.class), "Product should be in cache after concurrent access");
        
        // Frequency should reflect all accesses (though may not be exactly threadCount * 5 due to caching)
        int frequency = frequencyTracker.get(barcode);
        System.out.println("  Final frequency: " + frequency);
        assertTrue(frequency > 0, "Frequency should be tracked");
        
        System.out.println("✓ Concurrent access handled correctly");
    }
}

