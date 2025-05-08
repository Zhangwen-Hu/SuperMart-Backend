package com.example.supermartbackend.controller;

import com.example.supermartbackend.dto.ProductRequest;
import com.example.supermartbackend.dto.ProductResponse;
import com.example.supermartbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts() {
        // Check if user has admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            // Admin gets all products with complete information including wholesale price
            return ResponseEntity.ok(productService.getAllProductsForAdmin());
        } else {
            // Regular users only get in-stock products with limited information
            return ResponseEntity.ok(productService.getAllInStockProducts());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        // Check if user has admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            // Admin gets complete product information including wholesale price
            return ResponseEntity.ok(productService.getProductByIdForAdmin(id));
        } else {
            // Regular users get limited product information
            return ResponseEntity.ok(productService.getProductById(id));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(productService.addProduct(productRequest));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, productRequest));
    }
    
    @GetMapping("/profit/{count}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getTopProfitableProducts(@PathVariable int count) {
        return ResponseEntity.ok(productService.getTopProfitableProducts(count));
    }
    
    @GetMapping("/popular/{count}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getTopPopularProducts(@PathVariable int count) {
        return ResponseEntity.ok(productService.getTopPopularProducts(count));
    }
    
    @GetMapping("/recent/{count}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProductResponse>> getRecentlyPurchasedProducts(@PathVariable int count) {
        return ResponseEntity.ok(productService.getRecentlyPurchasedProducts(count));
    }
    
    @GetMapping("/frequent/{count}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProductResponse>> getFrequentlyPurchasedProducts(@PathVariable int count) {
        return ResponseEntity.ok(productService.getFrequentlyPurchasedProducts(count));
    }
} 