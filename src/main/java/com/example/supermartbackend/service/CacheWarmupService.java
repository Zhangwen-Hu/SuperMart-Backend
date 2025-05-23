package com.example.supermartbackend.service;

import com.example.supermartbackend.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupService {

    private final ProductServiceImpl productService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Starting cache warmup...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // Warm up most frequently accessed product caches
                log.info("Warming up products cache...");
                productService.getAllInStockProducts();
                
                // Warm up top products caches with common count values
                log.info("Warming up top products caches...");
                productService.getTopProfitableProducts(10);
                productService.getTopProfitableProducts(20);
                productService.getTopPopularProducts(10);
                productService.getTopPopularProducts(20);
                
                log.info("Cache warmup completed successfully");
            } catch (Exception e) {
                log.error("Error during cache warmup: {}", e.getMessage(), e);
            }
        });
    }
} 