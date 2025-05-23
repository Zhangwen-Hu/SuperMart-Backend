package com.example.supermartbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal wholesalePrice;
    private BigDecimal retailPrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 