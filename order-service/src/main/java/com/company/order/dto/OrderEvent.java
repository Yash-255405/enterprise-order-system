package com.company.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private Double total;
    private String status;
}