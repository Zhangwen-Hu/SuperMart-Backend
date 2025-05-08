package com.example.supermartbackend.service;

import com.example.supermartbackend.dto.OrderRequest;
import com.example.supermartbackend.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    
    OrderResponse placeOrder(OrderRequest orderRequest);
    
    List<OrderResponse> getAllOrders();
    
    OrderResponse getOrderById(Long id);
    
    OrderResponse cancelOrder(Long id);
    
    OrderResponse completeOrder(Long id);
} 