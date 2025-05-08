package com.example.supermartbackend.exception;

import com.example.supermartbackend.entity.Order.OrderStatus;

public class OrderModificationException extends RuntimeException {
    
    public OrderModificationException(String message) {
        super(message);
    }
    
    public OrderModificationException(Long orderId, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format("Cannot change order %d from %s to %s", 
                orderId, currentStatus.name(), targetStatus.name()));
    }
} 