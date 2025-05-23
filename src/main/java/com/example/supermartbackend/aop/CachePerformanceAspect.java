package com.example.supermartbackend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CachePerformanceAspect {

    private static final long SLOW_OPERATION_THRESHOLD_MS = 1000; // 1 second

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object monitorCacheablePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > SLOW_OPERATION_THRESHOLD_MS) {
                log.warn("Slow cacheable operation detected: {} took {} ms", methodName, executionTime);
            } else {
                log.debug("Cacheable operation completed: {} took {} ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Cacheable operation failed: {} took {} ms, error: {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Around("@annotation(org.springframework.cache.annotation.CacheEvict) || @annotation(org.springframework.cache.annotation.Caching)")
    public Object monitorCacheEvictPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.debug("Cache eviction operation completed: {} took {} ms", methodName, executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Cache eviction operation failed: {} took {} ms, error: {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }
} 