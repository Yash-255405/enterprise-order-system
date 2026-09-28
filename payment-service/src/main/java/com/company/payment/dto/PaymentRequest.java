package com.company.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Amount cannot be null")
    private Double amount;
}