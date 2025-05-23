package com.example.supermartbackend.service.impl;

import com.example.supermartbackend.dto.OrderItemRequest;
import com.example.supermartbackend.dto.OrderRequest;
import com.example.supermartbackend.dto.OrderResponse;
import com.example.supermartbackend.entity.Order;
import com.example.supermartbackend.entity.OrderItem;
import com.example.supermartbackend.entity.Product;
import com.example.supermartbackend.entity.User;
import com.example.supermartbackend.exception.InvalidCredentialsException;
import com.example.supermartbackend.exception.NotEnoughInventoryException;
import com.example.supermartbackend.exception.OrderModificationException;
import com.example.supermartbackend.repository.OrderRepository;
import com.example.supermartbackend.repository.ProductRepository;
import com.example.supermartbackend.repository.UserRepository;
import com.example.supermartbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Override
    @PreAuthorize("hasRole('USER')")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true),
        @CacheEvict(value = "user-orders", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"),
        @CacheEvict(value = {"products", "admin-products", "top-popular-products", "user-recent-products", "user-frequent-products"}, allEntries = true)
    })
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        User currentUser = getCurrentUser();
        
        Order order = new Order();
        order.setUser(currentUser);
        
        // Process each order item
        for (OrderItemRequest itemRequest : orderRequest.getOrder()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + itemRequest.getProductId()));
            
            // Check if product is in stock
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new NotEnoughInventoryException(product.getId(), itemRequest.getQuantity(), product.getQuantity());
            }
            
            // Reduce product quantity
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
            
            // Create and add order item
            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            order.addItem(orderItem);
        }
        
        // Save the order
        Order savedOrder = orderRepository.save(order);
        
        return mapToResponse(savedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        List<Order> orders;
        if (isAdmin) {
            // Admins can see all orders - cache this expensive operation
            orders = getAllOrdersCached();
        } else {
            // Users can only see their own orders
            String username = authentication.getName();
            orders = getUserOrdersCached(username);
        }
        
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "orders", key = "'all'")
    @Transactional(readOnly = true)
    public List<Order> getAllOrdersCached() {
        return orderRepository.findAll();
    }
    
    @Cacheable(value = "user-orders", key = "#username")
    @Transactional(readOnly = true)
    public List<Order> getUserOrdersCached(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        return orderRepository.findAllByUserId(user.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "'order-' + #id")
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Check authorization
        checkOrderAccess(order);
        
        return mapToResponse(order);
    }
    
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true),
        @CacheEvict(value = "user-orders", allEntries = true),
        @CacheEvict(value = {"products", "admin-products"}, allEntries = true)
    })
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Check authorization
        checkOrderAccess(order);
        
        // Check if order can be canceled
        if (order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new OrderModificationException(id, order.getStatus(), Order.OrderStatus.CANCELED);
        }
        
        // Return products to inventory
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.CANCELED);
        Order savedOrder = orderRepository.save(order);
        
        return mapToResponse(savedOrder);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true),
        @CacheEvict(value = "user-orders", allEntries = true)
    })
    public OrderResponse completeOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Check if order can be completed
        if (order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new OrderModificationException(id, order.getStatus(), Order.OrderStatus.COMPLETED);
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(order);
        
        return mapToResponse(savedOrder);
    }
    
    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        
        for (OrderItem item : order.getItems()) {
            BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            
            OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse(
                    item.getProduct().getId(),
                    item.getProductName(),
                    item.getProductDescription(),
                    item.getQuantity(),
                    item.getPrice(),
                    subtotal
            );
            
            itemResponses.add(itemResponse);
        }
        
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }
    
    private void checkOrderAccess(Order order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !order.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("You do not have permission to access this order");
        }
    }
} 