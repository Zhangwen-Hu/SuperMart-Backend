package com.example.supermartbackend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut for all service methods
     */
    @Pointcut("execution(* com.example.supermartbackend.service.*.*(..))")
    private void servicePointcut() {
        // Method is empty as this is just a Pointcut
    }

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.example.supermartbackend.controller.*.*(..))")
    private void controllerPointcut() {
        // Method is empty as this is just a Pointcut
    }

    /**
     * Pointcut for all repository methods
     */
    @Pointcut("execution(* com.example.supermartbackend.repository.*.*(..))")
    private void repositoryPointcut() {
        // Method is empty as this is just a Pointcut
    }

    /**
     * Advice that logs methods throwing exceptions
     */
    @AfterThrowing(pointcut = "servicePointcut() || controllerPointcut() || repositoryPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        String username = getCurrentUsername();
        log.error("Exception in {}.{}() with cause = '{}' and message = '{}'",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getCause() != null ? e.getCause() : "NULL",
                e.getMessage(), e);
        
        if (username != null) {
            log.error("User '{}' triggered an exception", username);
        }
    }

    /**
     * Advice that logs service method entry and exit
     */
    @Around("servicePointcut()")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "Service");
    }

    /**
     * Advice that logs controller method entry and exit
     */
    @Around("controllerPointcut()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "Controller");
    }

    /**
     * Helper method to log method execution
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String methodType) throws Throwable {
        String username = getCurrentUsername();
        
        if (log.isDebugEnabled()) {
            log.debug("[{}] Enter: {}.{}() with argument[s] = {}",
                    methodType,
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
            
            if (username != null) {
                log.debug("User '{}' is performing operation", username);
            }
        }
        
        try {
            Object result = joinPoint.proceed();
            
            if (log.isDebugEnabled()) {
                log.debug("[{}] Exit: {}.{}() with result = {}",
                        methodType,
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        result);
            }
            
            return result;
        } catch (IllegalArgumentException e) {
            log.error("[{}] Illegal argument: {} in {}.{}()",
                    methodType,
                    Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
            throw e;
        }
    }
    
    /**
     * Helper method to get the current authenticated username
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Error getting authenticated user: {}", e.getMessage());
        }
        return null;
    }
} 