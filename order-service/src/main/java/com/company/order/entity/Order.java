package com.company.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private String skuCode;

    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, unique = true)
    private String idempotencyKey;
}