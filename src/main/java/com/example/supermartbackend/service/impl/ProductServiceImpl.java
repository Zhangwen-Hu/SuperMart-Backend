package com.example.supermartbackend.service.impl;

import com.example.supermartbackend.dto.AdminProductResponse;
import com.example.supermartbackend.dto.ProductRequest;
import com.example.supermartbackend.dto.ProductResponse;
import com.example.supermartbackend.entity.Product;
import com.example.supermartbackend.entity.User;
import com.example.supermartbackend.repository.ProductRepository;
import com.example.supermartbackend.repository.UserRepository;
import com.example.supermartbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all-in-stock'")
    public List<ProductResponse> getAllInStockProducts() {
        return productRepository.findAllInStock().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'product-' + #id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "admin-products", key = "'admin-product-' + #id")
    public AdminProductResponse getProductByIdForAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToAdminResponse(product);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "admin-products", key = "'all-admin'")
    public List<AdminProductResponse> getAllProductsForAdmin() {
        return productRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = {"products", "admin-products", "top-profitable-products", "top-popular-products"}, allEntries = true)
    public ProductResponse addProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setWholesalePrice(productRequest.getWholesalePrice());
        product.setRetailPrice(productRequest.getRetailPrice());
        product.setQuantity(productRequest.getQuantity());
        
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = {"products", "admin-products", "top-profitable-products", "top-popular-products"}, allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (productRequest.getName() != null) {
            product.setName(productRequest.getName());
        }
        
        if (productRequest.getDescription() != null) {
            product.setDescription(productRequest.getDescription());
        }
        
        if (productRequest.getWholesalePrice() != null) {
            product.setWholesalePrice(productRequest.getWholesalePrice());
        }
        
        if (productRequest.getRetailPrice() != null) {
            product.setRetailPrice(productRequest.getRetailPrice());
        }
        
        if (productRequest.getQuantity() != null) {
            product.setQuantity(productRequest.getQuantity());
        }
        
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "top-profitable-products", key = "'top-profit-' + #count")
    public List<ProductResponse> getTopProfitableProducts(int count) {
        return productRepository.findTopXByProfit(count).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "top-popular-products", key = "'top-popular-' + #count")
    public List<ProductResponse> getTopPopularProducts(int count) {
        return productRepository.findTopXByPopularity(count).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    @Cacheable(value = "user-recent-products", key = "'recent-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '-' + #count")
    public List<ProductResponse> getRecentlyPurchasedProducts(int count) {
        User currentUser = getCurrentUser();
        return productRepository.findMostRecentlyPurchased(currentUser.getId(), count).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    @Cacheable(value = "user-frequent-products", key = "'frequent-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '-' + #count")
    public List<ProductResponse> getFrequentlyPurchasedProducts(int count) {
        User currentUser = getCurrentUser();
        return productRepository.findMostFrequentlyPurchased(currentUser.getId(), count).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
    
    private AdminProductResponse mapToAdminResponse(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getWholesalePrice(),
                product.getRetailPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
} 