package com.example.supermartbackend.service.impl;

import com.example.supermartbackend.dto.ProductResponse;
import com.example.supermartbackend.entity.Product;
import com.example.supermartbackend.entity.User;
import com.example.supermartbackend.repository.ProductRepository;
import com.example.supermartbackend.repository.UserRepository;
import com.example.supermartbackend.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class WatchlistServiceImpl implements WatchlistService {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getWatchlistProducts() {
        User currentUser = getCurrentUser();
        
        return currentUser.getWatchlist().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void addToWatchlist(Long productId) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product is already in watchlist
        if (currentUser.getWatchlist().contains(product)) {
            return;
        }
        
        currentUser.getWatchlist().add(product);
        userRepository.save(currentUser);
    }
    
    @Override
    @Transactional
    public void removeFromWatchlist(Long productId) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        currentUser.getWatchlist().remove(product);
        userRepository.save(currentUser);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getRetailPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
} 