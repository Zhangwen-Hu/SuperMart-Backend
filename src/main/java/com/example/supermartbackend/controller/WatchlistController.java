package com.example.supermartbackend.controller;

import com.example.supermartbackend.dto.ProductResponse;
import com.example.supermartbackend.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    
    private final WatchlistService watchlistService;
    
    @GetMapping("/products/all")
    public ResponseEntity<List<ProductResponse>> getWatchlistProducts() {
        return ResponseEntity.ok(watchlistService.getWatchlistProducts());
    }
    
    @PostMapping("/product/{productId}")
    public ResponseEntity<?> addToWatchlist(@PathVariable Long productId) {
        watchlistService.addToWatchlist(productId);
        return ResponseEntity.ok("Product added to watchlist successfully");
    }
    
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> removeFromWatchlist(@PathVariable Long productId) {
        watchlistService.removeFromWatchlist(productId);
        return ResponseEntity.ok("Product removed from watchlist successfully");
    }
} 