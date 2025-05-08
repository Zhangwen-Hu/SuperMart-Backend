package com.example.supermartbackend.service;

import com.example.supermartbackend.dto.ProductResponse;

import java.util.List;

public interface WatchlistService {
    
    List<ProductResponse> getWatchlistProducts();
    
    void addToWatchlist(Long productId);
    
    void removeFromWatchlist(Long productId);
} 