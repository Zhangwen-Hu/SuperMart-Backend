package com.example.supermartbackend.config;

import com.example.supermartbackend.entity.Order;
import com.example.supermartbackend.entity.OrderItem;
import com.example.supermartbackend.entity.Product;
import com.example.supermartbackend.entity.Role;
import com.example.supermartbackend.entity.User;
import com.example.supermartbackend.repository.OrderRepository;
import com.example.supermartbackend.repository.ProductRepository;
import com.example.supermartbackend.repository.RoleRepository;
import com.example.supermartbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInit implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing Database");
        initRoles();
        initUsers();
        initProducts();
        initOrders();
    }
    
    private void initRoles() {
        boolean userRoleExists = roleRepository.findByName(Role.ERole.ROLE_USER).isPresent();
        boolean adminRoleExists = roleRepository.findByName(Role.ERole.ROLE_ADMIN).isPresent();
        
        if (!userRoleExists && !adminRoleExists) {
            log.info("Initializing roles");
            Role adminRole = new Role();
            adminRole.setName(Role.ERole.ROLE_ADMIN);
            
            Role userRole = new Role();
            userRole.setName(Role.ERole.ROLE_USER);
            
            roleRepository.save(adminRole);
            roleRepository.save(userRole);
        }
    }
    
    private void initUsers() {
        boolean adminExists = userRepository.findByUsername("admin").isPresent();
        boolean userExists = userRepository.findByUsername("user").isPresent();
        
        if (!adminExists && !userExists) {
            log.info("Initializing users");
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@supermart.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEnabled(true);
            
            Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Admin Role not found."));
            
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(roleRepository.findByName(Role.ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: User Role not found.")));
            adminUser.setRoles(adminRoles);
            
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setEmail("user@supermart.com");
            regularUser.setPassword(passwordEncoder.encode("user123"));
            regularUser.setEnabled(true);
            
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: User Role not found."));
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            regularUser.setRoles(userRoles);
            
            User testUser = new User();
            testUser.setUsername("test");
            testUser.setEmail("test@supermart.com");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setEnabled(true);
            testUser.setRoles(userRoles);
            
            userRepository.save(adminUser);
            userRepository.save(regularUser);
            userRepository.save(testUser);
        }
    }
    
    private void initProducts() {
        try {
            List<Product> existingProducts = productRepository.findAllInStock();
            if (existingProducts.isEmpty()) {
                log.info("Initializing products");
                List<Product> products = Arrays.asList(
                    createProduct("iPhone 14 Pro", "Apple's flagship smartphone with A16 Bionic chip", new BigDecimal("999.99"), new BigDecimal("799.99"), 50),
                    createProduct("Samsung Galaxy S23", "Samsung's premium Android smartphone", new BigDecimal("899.99"), new BigDecimal("699.99"), 45),
                    createProduct("MacBook Pro M2", "Apple's professional laptop with M2 chip", new BigDecimal("1499.99"), new BigDecimal("1299.99"), 30),
                    createProduct("iPad Air", "Lightweight tablet for professionals", new BigDecimal("599.99"), new BigDecimal("499.99"), 60),
                    createProduct("Sony WH-1000XM5", "Noise cancelling headphones", new BigDecimal("399.99"), new BigDecimal("299.99"), 100),
                    createProduct("LG OLED C2 TV", "55-inch OLED 4K TV", new BigDecimal("1299.99"), new BigDecimal("999.99"), 25),
                    createProduct("PlayStation 5", "Sony's next-gen gaming console", new BigDecimal("499.99"), new BigDecimal("449.99"), 15),
                    createProduct("Xbox Series X", "Microsoft's powerful gaming console", new BigDecimal("499.99"), new BigDecimal("449.99"), 20),
                    createProduct("Dyson V12", "Cordless vacuum cleaner", new BigDecimal("649.99"), new BigDecimal("549.99"), 40),
                    createProduct("Nespresso Vertuo", "Coffee machine with multiple brew sizes", new BigDecimal("199.99"), new BigDecimal("149.99"), 70)
                );
                
                for (Product product : products) {
                    productRepository.save(product);
                }
            }
        } catch (Exception e) {
            log.error("Error initializing products: {}", e.getMessage());
        }
    }
    
    private void initOrders() {
        if (!userRepository.findByUsername("user").isPresent()) {
            log.info("Skipping order initialization - user account not found");
            return;
        }
        
        try {
            List<Product> products = productRepository.findAllInStock();
            if (products.isEmpty()) {
                log.info("Skipping order initialization - no products found");
                return;
            }
            
            User user = userRepository.findByUsername("user")
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            
            Order processingOrder = new Order();
            processingOrder.setUser(user);
            processingOrder.setStatus(Order.OrderStatus.PROCESSING);
            
            Product product1 = products.get(0);
            Product product2 = products.get(1);
            
            OrderItem item1 = new OrderItem(product1, 2);
            OrderItem item2 = new OrderItem(product2, 1);
            
            processingOrder.addItem(item1);
            processingOrder.addItem(item2);
            
            orderRepository.save(processingOrder);
            
            Order completedOrder = new Order();
            completedOrder.setUser(user);
            completedOrder.setStatus(Order.OrderStatus.COMPLETED);
            
            Product product3 = products.get(2);
            
            OrderItem item3 = new OrderItem(product3, 1);
            
            completedOrder.addItem(item3);
            
            orderRepository.save(completedOrder);
            
            log.info("Created test orders successfully");
        } catch (Exception e) {
            log.error("Error creating test orders: {}", e.getMessage());
        }
    }
    
    private Product createProduct(String name, String description, BigDecimal retailPrice, BigDecimal wholesalePrice, int quantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setRetailPrice(retailPrice);
        product.setWholesalePrice(wholesalePrice);
        product.setQuantity(quantity);
        return product;
    }
} 