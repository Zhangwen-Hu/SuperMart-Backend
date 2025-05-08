package com.example.supermartbackend.service;

import com.example.supermartbackend.dto.AdminProductResponse;
import com.example.supermartbackend.dto.ProductRequest;
import com.example.supermartbackend.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    
    List<ProductResponse> getAllInStockProducts();
    
    ProductResponse getProductById(Long id);
    
    // Admin-specific methods
    AdminProductResponse getProductByIdForAdmin(Long id);
    
    List<AdminProductResponse> getAllProductsForAdmin();
    
    ProductResponse addProduct(ProductRequest productRequest);
    
    ProductResponse updateProduct(Long id, ProductRequest productRequest);
    
    List<ProductResponse> getTopProfitableProducts(int count);
    
    List<ProductResponse> getTopPopularProducts(int count);
    
    List<ProductResponse> getRecentlyPurchasedProducts(int count);
    
    List<ProductResponse> getFrequentlyPurchasedProducts(int count);
} 