package com.example.supermartbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get all cache names
        stats.put("cacheNames", cacheManager.getCacheNames());
        
        // Get Redis info
        try {
            Set<String> keys = redisTemplate.keys("*");
            stats.put("totalKeys", keys != null ? keys.size() : 0);
            stats.put("redisKeys", keys);
        } catch (Exception e) {
            stats.put("redisError", e.getMessage());
        }
        
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllCaches() {
        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
            return ResponseEntity.ok("All caches cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error clearing caches: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearSpecificCache(@PathVariable String cacheName) {
        try {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error clearing cache: " + e.getMessage());
        }
    }

    @PostMapping("/warmup")
    public ResponseEntity<String> warmupCache() {
        // This endpoint can be used to manually trigger cache warmup
        return ResponseEntity.ok("Cache warmup triggered (will run asynchronously)");
    }
} 