package com.example.supermartbackend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Default TTL: 10 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Products cache - longer TTL since products don't change frequently
        cacheConfigurations.put("products", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Products for admin - shorter TTL due to sensitive data
        cacheConfigurations.put("admin-products", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Top products caches - longer TTL since rankings don't change frequently
        cacheConfigurations.put("top-profitable-products", defaultCacheConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put("top-popular-products", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // User-specific caches - shorter TTL
        cacheConfigurations.put("user-recent-products", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("user-frequent-products", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Orders cache - short TTL due to frequent updates
        cacheConfigurations.put("orders", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("user-orders", defaultCacheConfig.entryTtl(Duration.ofMinutes(3)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
} 