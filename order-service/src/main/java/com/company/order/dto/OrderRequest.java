package com.company.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Total cannot be null")
    @Positive(message = "Total must be greater than zero")
    private Double total;

    @NotNull(message = "SKU code cannot be null")
    private String skuCode;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
    private String idempotencyKey;
}